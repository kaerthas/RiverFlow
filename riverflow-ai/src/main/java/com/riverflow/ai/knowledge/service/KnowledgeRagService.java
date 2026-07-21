package com.riverflow.ai.knowledge.service;

import com.riverflow.ai.config.AiProperties;
import com.riverflow.ai.knowledge.embedding.EmbeddingClient;
import com.riverflow.ai.knowledge.embedding.EmbeddingClientFactory;
import com.riverflow.ai.knowledge.entity.AiVectorCollection;
import com.riverflow.ai.knowledge.vector.VectorDocument;
import com.riverflow.ai.knowledge.vector.VectorStoreProvider;
import com.riverflow.ai.knowledge.vector.VectorStoreProviderFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RAG 检索服务
 *
 * <p>基于向量语义检索，为 AI 生成链路提供相关知识上下文。</p>
 */
@Slf4j
@Service
public class KnowledgeRagService {

    private final AiProperties aiProperties;
    private final EmbeddingClientFactory embeddingClientFactory;
    private final VectorStoreProviderFactory vectorStoreProviderFactory;
    private final VectorCollectionResolver collectionResolver;

    @Autowired
    public KnowledgeRagService(AiProperties aiProperties,
                               EmbeddingClientFactory embeddingClientFactory,
                               VectorStoreProviderFactory vectorStoreProviderFactory,
                               VectorCollectionResolver collectionResolver) {
        this.aiProperties = aiProperties;
        this.embeddingClientFactory = embeddingClientFactory;
        this.vectorStoreProviderFactory = vectorStoreProviderFactory;
        this.collectionResolver = collectionResolver;
    }

    /**
     * 语义检索
     *
     * @param query 查询文本
     * @param collectionId 指定集合配置ID，优先使用
     * @param collection 指定集合名称，兼容字段
     * @param topK Top-K
     * @param minScore 最低相似度
     * @return 相关文档块
     */
    public List<VectorDocument> search(String query, Long collectionId, String collection, Integer topK, Double minScore) {
        if (!aiProperties.getKnowledge().getRag().isEnabled()) {
            log.debug("RAG 未启用，跳过语义检索");
            return Collections.emptyList();
        }
        if (!StringUtils.hasText(query)) {
            return Collections.emptyList();
        }

        AiProperties.RagConfig ragConfig = aiProperties.getKnowledge().getRag();
        AiVectorCollection collectionConfig = collectionResolver.resolve(collectionId, collection);
        String targetCollection = collectionConfig.getCollection();
        int k = topK != null ? topK : ragConfig.getTopK();
        double score = minScore != null ? minScore : ragConfig.getMinScore();

        try {
            EmbeddingClient embeddingClient = embeddingClientFactory.create(collectionConfig);
            VectorStoreProvider provider = vectorStoreProviderFactory.getProvider(collectionConfig.getStoreType());

            float[] queryVector = embeddingClient.embed(query);
            List<VectorDocument> results = provider.search(targetCollection, queryVector, k, score);
            log.debug("RAG 语义检索完成: query={}, collection={}, results={}", query, targetCollection, results.size());
            return results;
        } catch (Exception e) {
            log.warn("RAG 语义检索失败，将返回空结果: query={}", query, e);
            return Collections.emptyList();
        }
    }

    /**
     * 语义检索（按集合名称，兼容旧接口）
     */
    public List<VectorDocument> search(String query, String collection, Integer topK, Double minScore) {
        return search(query, null, collection, topK, minScore);
    }

    /**
     * 按来源类型分组检索
     */
    public Map<String, List<VectorDocument>> searchGrouped(String query, Long collectionId, String collection, Integer topK, Double minScore) {
        List<VectorDocument> docs = search(query, collectionId, collection, topK, minScore);
        Map<String, List<VectorDocument>> grouped = new HashMap<>();
        for (VectorDocument doc : docs) {
            String sourceType = "other";
            if (doc.getMetadata() != null && doc.getMetadata().get("sourceType") != null) {
                sourceType = String.valueOf(doc.getMetadata().get("sourceType"));
            }
            grouped.computeIfAbsent(sourceType, k -> new ArrayList<>()).add(doc);
        }
        return grouped;
    }

    /**
     * 按来源类型分组检索（兼容旧接口）
     */
    public Map<String, List<VectorDocument>> searchGrouped(String query, String collection, Integer topK, Double minScore) {
        return searchGrouped(query, null, collection, topK, minScore);
    }
}
