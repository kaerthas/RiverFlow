package com.riverflow.ai.knowledge.service;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.riverflow.ai.config.AiProperties;
import com.riverflow.ai.knowledge.chunk.ChunkOptions;
import com.riverflow.ai.knowledge.chunk.DocumentChunker;
import com.riverflow.ai.knowledge.embedding.EmbeddingClient;
import com.riverflow.ai.knowledge.embedding.EmbeddingClientFactory;
import com.riverflow.ai.knowledge.entity.AiKnowledgeChunk;
import com.riverflow.ai.knowledge.entity.AiKnowledgeDoc;
import com.riverflow.ai.knowledge.entity.AiVectorCollection;
import com.riverflow.ai.knowledge.entity.ApiCatalog;
import com.riverflow.ai.knowledge.entity.Datasource;
import com.riverflow.ai.knowledge.entity.FlowDefinition;
import com.riverflow.ai.knowledge.mapper.AiKnowledgeChunkMapper;
import com.riverflow.ai.knowledge.mapper.AiKnowledgeDocMapper;
import com.riverflow.ai.knowledge.mapper.AiKnowledgeMapper;
import com.riverflow.ai.knowledge.mapper.AiVectorCollectionMapper;
import com.riverflow.ai.knowledge.vector.DistanceMetric;
import com.riverflow.ai.knowledge.vector.VectorDocument;
import com.riverflow.ai.knowledge.vector.VectorStoreProvider;
import com.riverflow.ai.knowledge.vector.VectorStoreProviderFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 知识库索引服务
 *
 * <p>负责文档分块、向量化、入库，以及从源表重建索引。</p>
 */
@Slf4j
@Service
public class KnowledgeIndexingService {

    private static final int BATCH_SIZE = 64;

    private final AiProperties aiProperties;
    private final DocumentChunker documentChunker;
    private final EmbeddingClientFactory embeddingClientFactory;
    private final VectorStoreProviderFactory vectorStoreProviderFactory;
    private final AiKnowledgeDocMapper docMapper;
    private final AiKnowledgeChunkMapper chunkMapper;
    private final AiVectorCollectionMapper collectionMapper;
    private final AiKnowledgeMapper knowledgeMapper;

    @Autowired
    public KnowledgeIndexingService(AiProperties aiProperties, DocumentChunker documentChunker,
                                     EmbeddingClientFactory embeddingClientFactory,
                                     VectorStoreProviderFactory vectorStoreProviderFactory,
                                     AiKnowledgeDocMapper docMapper,
                                     AiKnowledgeChunkMapper chunkMapper,
                                     AiVectorCollectionMapper collectionMapper,
                                     AiKnowledgeMapper knowledgeMapper) {
        this.aiProperties = aiProperties;
        this.documentChunker = documentChunker;
        this.embeddingClientFactory = embeddingClientFactory;
        this.vectorStoreProviderFactory = vectorStoreProviderFactory;
        this.docMapper = docMapper;
        this.chunkMapper = chunkMapper;
        this.collectionMapper = collectionMapper;
        this.knowledgeMapper = knowledgeMapper;
    }

    /**
     * 单条文档索引
     */
    @Transactional(rollbackFor = Exception.class)
    public void indexDoc(AiKnowledgeDoc doc) {
        if (doc == null || !StringUtils.hasText(doc.getContent())) {
            return;
        }
        String collection = resolveCollection(doc.getCollection());
        doc.setCollection(collection);
        doc.setVectorStatus(1); // 索引中
        saveOrUpdateDoc(doc);

        try {
            // 删除旧分块
            chunkMapper.deleteByDocId(doc.getId());

            // 分块
            ChunkOptions options = buildChunkOptions();
            List<String> chunks = documentChunker.chunk(doc.getContent(), options);
            if (chunks.isEmpty()) {
                doc.setVectorStatus(3);
                doc.setChunkCount(0);
                docMapper.updateById(doc);
                return;
            }

            // 保存分块
            List<AiKnowledgeChunk> chunkEntities = new ArrayList<>();
            for (int i = 0; i < chunks.size(); i++) {
                AiKnowledgeChunk chunk = new AiKnowledgeChunk();
                chunk.setDocId(doc.getId());
                chunk.setChunkIndex(i);
                chunk.setContent(chunks.get(i));
                chunk.setMetadata(JSON.toJSONString(buildChunkMetadata(doc, i)));
                chunkEntities.add(chunk);
            }
            for (AiKnowledgeChunk chunk : chunkEntities) {
                chunkMapper.insert(chunk);
            }

            // 向量化
            EmbeddingClient embeddingClient = embeddingClientFactory.create();
            int dimension = embeddingClient.dimension();
            List<float[]> embeddings = embeddingClient.embed(chunks);

            // 确保集合存在
            VectorStoreProvider provider = vectorStoreProviderFactory.getProvider();
            provider.createCollection(collection, dimension, DistanceMetric.COSINE);

            // 写入向量库
            List<VectorDocument> vectorDocs = new ArrayList<>(chunks.size());
            for (int i = 0; i < chunks.size(); i++) {
                VectorDocument vd = new VectorDocument();
                vd.setId(buildVectorId(doc.getId(), i));
                vd.setCollection(collection);
                vd.setDocId(String.valueOf(doc.getId()));
                vd.setChunkIndex(i);
                vd.setContent(chunks.get(i));
                vd.setEmbedding(embeddings.get(i));
                vd.setMetadata(buildVectorMetadata(doc, i));
                vectorDocs.add(vd);
            }
            upsertInBatches(provider, collection, vectorDocs);

            doc.setChunkCount(chunks.size());
            doc.setVectorStatus(2); // 已索引
            docMapper.updateById(doc);
            log.info("知识文档索引完成: docId={}, chunks={}, collection={}", doc.getId(), chunks.size(), collection);
        } catch (Exception e) {
            log.error("知识文档索引失败: docId={}", doc.getId(), e);
            doc.setVectorStatus(3); // 失败
            docMapper.updateById(doc);
            throw new RuntimeException("知识文档索引失败", e);
        }
    }

    /**
     * 重建指定集合或全部集合
     */
    public void rebuildCollection(String collection) {
        if (!StringUtils.hasText(collection)) {
            collection = resolveCollection(null);
        }
        log.info("开始重建知识库索引: collection={}", collection);

        // 清空旧文档
        LambdaQueryWrapper<AiKnowledgeDoc> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiKnowledgeDoc::getCollection, collection);
        List<AiKnowledgeDoc> oldDocs = docMapper.selectList(wrapper);
        for (AiKnowledgeDoc oldDoc : oldDocs) {
            deleteDoc(oldDoc.getId());
        }

        // 从源表重建
        rebuildFromFlows(collection);
        rebuildFromApis(collection);
        rebuildFromDatasources(collection);
        rebuildFromDynamicTables(collection);

        log.info("知识库索引重建完成: collection={}", collection);
    }

    /**
     * 删除文档（MySQL + 向量库）
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteDoc(Long docId) {
        AiKnowledgeDoc doc = docMapper.selectById(docId);
        if (doc == null) {
            return;
        }
        String collection = resolveCollection(doc.getCollection());
        VectorStoreProvider provider = vectorStoreProviderFactory.getProvider();

        // 查询分块确定 vector id
        List<AiKnowledgeChunk> chunks = chunkMapper.selectByDocId(docId);
        List<String> vectorIds = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            vectorIds.add(buildVectorId(docId, i));
        }
        if (!vectorIds.isEmpty()) {
            try {
                provider.deleteByIds(collection, vectorIds);
            } catch (Exception e) {
                log.warn("删除向量记录失败: docId={}, collection={}", docId, collection, e);
            }
        }
        chunkMapper.deleteByDocId(docId);
        docMapper.deleteById(docId);
    }

    private void rebuildFromFlows(String collection) {
        List<FlowDefinition> flows = knowledgeMapper.searchFlows(List.of(), 10000);
        for (FlowDefinition flow : flows) {
            if (flow.getDelFlag() != null && flow.getDelFlag() == 1) {
                continue;
            }
            AiKnowledgeDoc doc = new AiKnowledgeDoc();
            doc.setTitle(flow.getFlowName());
            doc.setSourceType("flow");
            doc.setSourceId(String.valueOf(flow.getId()));
            doc.setContent(buildFlowContent(flow));
            doc.setCollection(collection);
            doc.setEnabled(1);
            indexDoc(doc);
        }
    }

    private void rebuildFromApis(String collection) {
        List<ApiCatalog> apis = knowledgeMapper.searchApis(List.of(), 10000);
        for (ApiCatalog api : apis) {
            if (api.getDelFlag() != null && api.getDelFlag() == 1) {
                continue;
            }
            AiKnowledgeDoc doc = new AiKnowledgeDoc();
            doc.setTitle(api.getApiName());
            doc.setSourceType("api");
            doc.setSourceId(String.valueOf(api.getId()));
            doc.setContent(buildApiContent(api));
            doc.setCollection(collection);
            doc.setEnabled(1);
            indexDoc(doc);
        }
    }

    private void rebuildFromDatasources(String collection) {
        List<Datasource> datasources = knowledgeMapper.searchDatasources(List.of(), 10000);
        for (Datasource ds : datasources) {
            if (ds.getDelFlag() != null && ds.getDelFlag() == 1) {
                continue;
            }
            AiKnowledgeDoc doc = new AiKnowledgeDoc();
            doc.setTitle(ds.getDsName());
            doc.setSourceType("datasource");
            doc.setSourceId(String.valueOf(ds.getId()));
            doc.setContent(buildDatasourceContent(ds));
            doc.setCollection(collection);
            doc.setEnabled(1);
            indexDoc(doc);
        }
    }

    private void rebuildFromDynamicTables(String collection) {
        // 动态表数据源数量可能较大，通过 knowledgeMapper 的扩展方法查询
        // 当前 knowledgeMapper 未提供动态表查询，可后续补充；这里预留扩展点
    }

    private String buildFlowContent(FlowDefinition flow) {
        StringBuilder sb = new StringBuilder();
        sb.append("流程名称：").append(flow.getFlowName()).append("\n");
        sb.append("流程编码：").append(flow.getFlowCode()).append("\n");
        if (StringUtils.hasText(flow.getTriggerType())) {
            sb.append("触发方式：").append(flow.getTriggerType()).append("\n");
        }
        if (StringUtils.hasText(flow.getTriggerConfig())) {
            sb.append("触发配置：").append(flow.getTriggerConfig()).append("\n");
        }
        if (StringUtils.hasText(flow.getExecutionMode())) {
            sb.append("执行模式：").append(flow.getExecutionMode()).append("\n");
        }
        if (StringUtils.hasText(flow.getGraphJson())) {
            sb.append("流程图：").append(flow.getGraphJson()).append("\n");
        }
        return sb.toString();
    }

    private String buildApiContent(ApiCatalog api) {
        StringBuilder sb = new StringBuilder();
        sb.append("接口名称：").append(api.getApiName()).append("\n");
        sb.append("接口编码：").append(api.getApiCode()).append("\n");
        sb.append("接口类型：").append(api.getApiType()).append("\n");
        sb.append("请求方式：").append(api.getMethod()).append("\n");
        sb.append("请求地址：").append(api.getUrl()).append("\n");
        return sb.toString();
    }

    private String buildDatasourceContent(Datasource ds) {
        StringBuilder sb = new StringBuilder();
        sb.append("数据源名称：").append(ds.getDsName()).append("\n");
        sb.append("数据源编码：").append(ds.getDsCode()).append("\n");
        sb.append("数据库类型：").append(ds.getDbType()).append("\n");
        return sb.toString();
    }

    private void saveOrUpdateDoc(AiKnowledgeDoc doc) {
        if (doc.getId() == null) {
            doc.setEnabled(doc.getEnabled() != null ? doc.getEnabled() : 1);
            docMapper.insert(doc);
        } else {
            docMapper.updateById(doc);
        }
    }

    private ChunkOptions buildChunkOptions() {
        AiProperties.ChunkConfig config = aiProperties.getKnowledge().getChunk();
        ChunkOptions options = new ChunkOptions();
        options.setSize(config.getSize());
        options.setOverlap(config.getOverlap());
        options.setMaxChunks(config.getMaxChunks());
        return options;
    }

    private Map<String, Object> buildChunkMetadata(AiKnowledgeDoc doc, int chunkIndex) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("docId", doc.getId());
        metadata.put("sourceType", doc.getSourceType());
        metadata.put("sourceId", doc.getSourceId());
        metadata.put("title", doc.getTitle());
        metadata.put("chunkIndex", chunkIndex);
        return metadata;
    }

    private Map<String, Object> buildVectorMetadata(AiKnowledgeDoc doc, int chunkIndex) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("sourceType", doc.getSourceType());
        metadata.put("sourceId", doc.getSourceId());
        metadata.put("title", doc.getTitle());
        metadata.put("chunkIndex", chunkIndex);
        return metadata;
    }

    private String buildVectorId(Long docId, int chunkIndex) {
        return docId + "_" + chunkIndex;
    }

    private String resolveCollection(String collection) {
        if (StringUtils.hasText(collection)) {
            return collection;
        }
        AiProperties.VectorStoreConfig config = aiProperties.getKnowledge().getVectorStore();
        if (StringUtils.hasText(config.getDefaultCollection())) {
            return config.getDefaultCollection();
        }
        return "riverflow_default";
    }

    private void upsertInBatches(VectorStoreProvider provider, String collection, List<VectorDocument> vectorDocs) {
        for (int i = 0; i < vectorDocs.size(); i += BATCH_SIZE) {
            List<VectorDocument> batch = vectorDocs.subList(i, Math.min(i + BATCH_SIZE, vectorDocs.size()));
            provider.upsert(collection, batch);
        }
    }
}
