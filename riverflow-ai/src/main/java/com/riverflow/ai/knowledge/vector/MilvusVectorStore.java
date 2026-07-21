package com.riverflow.ai.knowledge.vector;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.riverflow.ai.config.AiProperties;
import com.riverflow.ai.knowledge.entity.AiVectorCollection;
import com.riverflow.ai.knowledge.mapper.AiVectorCollectionMapper;
import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.common.DataType;
import io.milvus.v2.common.IndexParam;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.collection.request.DescribeCollectionReq;
import io.milvus.v2.service.collection.request.DropCollectionReq;
import io.milvus.v2.service.collection.request.GetCollectionStatsReq;
import io.milvus.v2.service.collection.request.HasCollectionReq;
import io.milvus.v2.service.collection.request.LoadCollectionReq;
import io.milvus.v2.service.index.request.CreateIndexReq;
import io.milvus.v2.service.index.request.DescribeIndexReq;
import io.milvus.v2.service.vector.request.DeleteReq;
import io.milvus.v2.service.vector.request.SearchReq;
import io.milvus.v2.service.vector.request.UpsertReq;
import io.milvus.v2.service.vector.request.data.FloatVec;
import io.milvus.v2.service.vector.response.SearchResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Milvus 向量存储实现
 *
 * <p>支持全局配置和按集合配置动态连接 Milvus 实例。</p>
 */
@Slf4j
@Component
public class MilvusVectorStore implements VectorStoreProvider {

    public static final String TYPE = "milvus";
    private static final String ID_FIELD = "id";
    private static final String DOC_ID_FIELD = "doc_id";
    private static final String CHUNK_INDEX_FIELD = "chunk_index";
    private static final String CONTENT_FIELD = "content";
    private static final String METADATA_FIELD = "metadata";
    private static final String EMBEDDING_FIELD = "embedding";

    private final AiProperties aiProperties;
    private final AiVectorCollectionMapper collectionMapper;
    private MilvusClientV2 globalClient;
    private final Map<String, MilvusClientV2> dynamicClients = new ConcurrentHashMap<>();
    private final Gson gson = new Gson();

    @Autowired
    public MilvusVectorStore(AiProperties aiProperties, AiVectorCollectionMapper collectionMapper) {
        this.aiProperties = aiProperties;
        this.collectionMapper = collectionMapper;
    }

    @PostConstruct
    public void init() {
        if (!TYPE.equalsIgnoreCase(aiProperties.getKnowledge().getVectorStore().getType())) {
            return;
        }
        AiProperties.MilvusConfig config = aiProperties.getKnowledge().getVectorStore().getMilvus();
        this.globalClient = createClient(config.getHost(), config.getPort(), config.getToken(), config.isSecure());
        log.info("Milvus 全局客户端初始化完成: uri={}", buildUri(config.getHost(), config.getPort(), config.isSecure()));
    }

    @PreDestroy
    public void destroy() {
        closeClient(globalClient);
        for (MilvusClientV2 client : dynamicClients.values()) {
            closeClient(client);
        }
        dynamicClients.clear();
    }

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public void createCollection(String collection, int dimension, DistanceMetric metric) {
        MilvusClientV2 client = getClient(collection);
        if (collectionExists(collection)) {
            if (isCollectionSchemaMatch(collection, dimension)) {
                return;
            }
            long numOfEntities = getCollectionNumOfEntities(collection);
            if (numOfEntities == 0) {
                log.warn("Milvus 集合 {} schema 与当前配置不一致且为空，自动删除并重建", collection);
                client.dropCollection(DropCollectionReq.builder()
                        .collectionName(collection)
                        .build());
            } else {
                throw new IllegalStateException(String.format(
                        "Milvus 集合 %s 已存在，但 schema 与当前配置不一致（维度或字段类型不匹配）。" +
                                "请删除该集合后重建，或调整 Embedding 模型/集合配置维度一致。",
                        collection));
            }
        }
        String indexType = aiProperties.getKnowledge().getVectorStore().getMilvus().getIndexType();
        List<CreateCollectionReq.FieldSchema> fields = new ArrayList<>();
        fields.add(CreateCollectionReq.FieldSchema.builder()
                .name(ID_FIELD)
                .dataType(DataType.VarChar)
                .isPrimaryKey(true)
                .maxLength(128)
                .autoID(false)
                .build());
        fields.add(CreateCollectionReq.FieldSchema.builder()
                .name(DOC_ID_FIELD)
                .dataType(DataType.VarChar)
                .maxLength(128)
                .build());
        fields.add(CreateCollectionReq.FieldSchema.builder()
                .name(CHUNK_INDEX_FIELD)
                .dataType(DataType.Int64)
                .build());
        fields.add(CreateCollectionReq.FieldSchema.builder()
                .name(CONTENT_FIELD)
                .dataType(DataType.VarChar)
                .maxLength(65535)
                .build());
        fields.add(CreateCollectionReq.FieldSchema.builder()
                .name(METADATA_FIELD)
                .dataType(DataType.VarChar)
                .maxLength(65535)
                .build());
        fields.add(CreateCollectionReq.FieldSchema.builder()
                .name(EMBEDDING_FIELD)
                .dataType(DataType.FloatVector)
                .dimension(dimension)
                .build());
        CreateCollectionReq.CollectionSchema schema = CreateCollectionReq.CollectionSchema.builder()
                .fieldSchemaList(fields)
                .build();
        IndexParam indexParam = toMilvusIndexParam(indexType, metric, dimension);
        CreateCollectionReq req = CreateCollectionReq.builder()
                .collectionName(collection)
                .collectionSchema(schema)
                .indexParams(List.of(indexParam))
                .metricType(toMilvusMetric(metric))
                .build();
        client.createCollection(req);
        // 显式创建向量索引，确保后续 load/search 可用
        client.createIndex(CreateIndexReq.builder()
                .collectionName(collection)
                .indexParams(List.of(indexParam))
                .build());
        log.info("Milvus 集合创建完成: collection={}, dimension={}, metric={}, index={}",
                collection, dimension, metric, indexType);
    }

    @Override
    public void upsert(String collection, List<VectorDocument> documents) {
        MilvusClientV2 client = getClient(collection);
        List<JsonObject> rows = documents.stream()
                .map(this::toMilvusRow)
                .collect(Collectors.toList());
        client.upsert(UpsertReq.builder()
                .collectionName(collection)
                .data(rows)
                .build());
    }

    @Override
    public List<VectorDocument> search(String collection, float[] vector, int topK, double minScore) {
        MilvusClientV2 client = getClient(collection);
        ensureVectorIndex(client, collection, vector.length);
        client.loadCollection(LoadCollectionReq.builder()
                .collectionName(collection)
                .build());
        SearchReq req = SearchReq.builder()
                .collectionName(collection)
                .data(Collections.singletonList(new FloatVec(vector)))
                .topK(topK)
                .outputFields(List.of(ID_FIELD, DOC_ID_FIELD, CHUNK_INDEX_FIELD, CONTENT_FIELD, METADATA_FIELD))
                .build();
        SearchResp resp = client.search(req);
        List<SearchResp.SearchResult> results = resp.getSearchResults().get(0);
        List<VectorDocument> docs = new ArrayList<>();
        for (SearchResp.SearchResult r : results) {
            Float score = r.getScore();
            if (score != null && score < minScore) {
                continue;
            }
            Map<String, Object> entity = r.getEntity();
            VectorDocument doc = new VectorDocument();
            doc.setId(toString(entity.get(ID_FIELD)));
            doc.setCollection(collection);
            doc.setDocId(toString(entity.get(DOC_ID_FIELD)));
            doc.setChunkIndex(parseInt(entity.get(CHUNK_INDEX_FIELD)));
            doc.setContent(toString(entity.get(CONTENT_FIELD)));
            doc.setMetadata(parseMetadata(entity.get(METADATA_FIELD)));
            doc.setScore(score != null ? score.doubleValue() : null);
            docs.add(doc);
        }
        return docs;
    }

    @Override
    public void deleteByIds(String collection, List<String> ids) {
        MilvusClientV2 client = getClient(collection);
        client.delete(DeleteReq.builder()
                .collectionName(collection)
                .ids(new ArrayList<>(ids))
                .build());
    }

    @Override
    public void dropCollection(String collection) {
        MilvusClientV2 client = getClient(collection);
        if (!collectionExists(collection)) {
            return;
        }
        client.dropCollection(DropCollectionReq.builder()
                .collectionName(collection)
                .build());
    }

    @Override
    public boolean collectionExists(String collection) {
        MilvusClientV2 client = getClient(collection);
        return client.hasCollection(HasCollectionReq.builder()
                .collectionName(collection)
                .build());
    }

    /**
     * 根据集合配置测试连接
     */
    public boolean testConnection(AiVectorCollection collection) {
        MilvusClientV2 client = createClient(collection);
        try {
            client.hasCollection(HasCollectionReq.builder()
                    .collectionName(collection.getCollection())
                    .build());
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Milvus 连接失败: " + e.getMessage(), e);
        }
    }

    private MilvusClientV2 getClient(String collection) {
        if (!StringUtils.hasText(collection)) {
            return ensureGlobalClient();
        }
        AiVectorCollection config = collectionMapper.selectByCollection(collection);
        if (config == null || !TYPE.equalsIgnoreCase(config.getStoreType())) {
            return ensureGlobalClient();
        }
        return createClient(config);
    }

    private MilvusClientV2 createClient(AiVectorCollection config) {
        AiProperties.MilvusConfig global = aiProperties.getKnowledge().getVectorStore().getMilvus();
        String host = StringUtils.hasText(config.getMilvusHost()) ? config.getMilvusHost() : global.getHost();
        int port = config.getMilvusPort() != null ? config.getMilvusPort() : global.getPort();
        String token = StringUtils.hasText(config.getMilvusToken()) ? config.getMilvusToken() : global.getToken();
        boolean secure = config.getMilvusSecure() != null ? config.getMilvusSecure() == 1 : global.isSecure();
        return createClient(host, port, token, secure);
    }

    private MilvusClientV2 createClient(String host, int port, String token, boolean secure) {
        String cacheKey = host + ":" + port + ":" + (token != null ? token : "") + ":" + secure;
        return dynamicClients.computeIfAbsent(cacheKey, k -> {
            ConnectConfig.ConnectConfigBuilder builder = ConnectConfig.builder()
                    .uri(buildUri(host, port, secure));
            if (StringUtils.hasText(token)) {
                builder.token(token);
            }
            MilvusClientV2 client = new MilvusClientV2(builder.build());
            log.info("Milvus 动态客户端创建完成: uri={}", buildUri(host, port, secure));
            return client;
        });
    }

    private MilvusClientV2 ensureGlobalClient() {
        if (globalClient == null) {
            throw new IllegalStateException("Milvus 全局客户端未初始化，请检查配置 riverflow.ai.knowledge.vector-store.type");
        }
        return globalClient;
    }

    private long getCollectionNumOfEntities(String collection) {
        MilvusClientV2 client = getClient(collection);
        GetCollectionStatsReq req = GetCollectionStatsReq.builder()
                .collectionName(collection)
                .build();
        io.milvus.v2.service.collection.response.GetCollectionStatsResp resp = client.getCollectionStats(req);
        return resp.getNumOfEntities() != null ? resp.getNumOfEntities() : 0L;
    }

    private int describeCollectionDimension(String collection) {
        MilvusClientV2 client = getClient(collection);
        DescribeCollectionReq req = DescribeCollectionReq.builder()
                .collectionName(collection)
                .build();
        io.milvus.v2.service.collection.response.DescribeCollectionResp resp = client.describeCollection(req);
        CreateCollectionReq.FieldSchema field = resp.getCollectionSchema().getField(EMBEDDING_FIELD);
        if (field == null || field.getDimension() == null) {
            throw new IllegalStateException("无法获取 Milvus 集合 " + collection + " 的向量维度");
        }
        return field.getDimension();
    }

    private void ensureVectorIndex(MilvusClientV2 client, String collection, int dimension) {
        try {
            client.describeIndex(DescribeIndexReq.builder()
                    .collectionName(collection)
                    .fieldName(EMBEDDING_FIELD)
                    .build());
        } catch (Exception e) {
            log.warn("Milvus 集合 {} 向量索引不存在，正在创建", collection);
            AiVectorCollection config = collectionMapper.selectByCollection(collection);
            DistanceMetric metric = DistanceMetric.COSINE;
            if (config != null && StringUtils.hasText(config.getDistanceMetric())) {
                try {
                    metric = DistanceMetric.valueOf(config.getDistanceMetric().trim().toUpperCase());
                } catch (IllegalArgumentException ignored) {
                }
            }
            String indexType = aiProperties.getKnowledge().getVectorStore().getMilvus().getIndexType();
            IndexParam indexParam = toMilvusIndexParam(indexType, metric, dimension);
            client.createIndex(CreateIndexReq.builder()
                    .collectionName(collection)
                    .indexParams(List.of(indexParam))
                    .build());
        }
    }

    private boolean isCollectionSchemaMatch(String collection, int dimension) {
        try {
            MilvusClientV2 client = getClient(collection);
            DescribeCollectionReq req = DescribeCollectionReq.builder()
                    .collectionName(collection)
                    .build();
            io.milvus.v2.service.collection.response.DescribeCollectionResp resp = client.describeCollection(req);
            CreateCollectionReq.CollectionSchema schema = resp.getCollectionSchema();
            CreateCollectionReq.FieldSchema idField = schema.getField(ID_FIELD);
            CreateCollectionReq.FieldSchema embeddingField = schema.getField(EMBEDDING_FIELD);
            if (idField == null || idField.getDataType() != DataType.VarChar) {
                log.warn("Milvus 集合 {} id 字段类型不匹配: expected=VarChar, actual={}",
                        collection, idField != null ? idField.getDataType() : "null");
                return false;
            }
            if (embeddingField == null || embeddingField.getDimension() == null || embeddingField.getDimension() != dimension) {
                log.warn("Milvus 集合 {} embedding 维度不匹配: expected={}, actual={}",
                        collection, dimension, embeddingField != null ? embeddingField.getDimension() : "null");
                return false;
            }
            return true;
        } catch (Exception e) {
            log.warn("检查 Milvus 集合 {} schema 失败", collection, e);
            return false;
        }
    }

    private void closeClient(MilvusClientV2 client) {
        if (client != null) {
            try {
                client.close();
            } catch (Exception e) {
                log.warn("关闭 Milvus 客户端失败", e);
            }
        }
    }

    private String buildUri(AiProperties.MilvusConfig config) {
        return buildUri(config.getHost(), config.getPort(), config.isSecure());
    }

    private String buildUri(String host, int port, boolean secure) {
        String protocol = secure ? "https" : "http";
        return protocol + "://" + host + ":" + port;
    }

    private JsonObject toMilvusRow(VectorDocument doc) {
        JsonObject row = new JsonObject();
        row.addProperty(ID_FIELD, doc.getId());
        row.addProperty(DOC_ID_FIELD, doc.getDocId());
        row.addProperty(CHUNK_INDEX_FIELD, doc.getChunkIndex());
        row.addProperty(CONTENT_FIELD, doc.getContent());
        row.addProperty(METADATA_FIELD, gson.toJson(doc.getMetadata() != null ? doc.getMetadata() : new HashMap<>()));
        row.add(EMBEDDING_FIELD, gson.toJsonTree(doc.getEmbedding()));
        return row;
    }

    private String toMilvusMetric(DistanceMetric metric) {
        return switch (metric) {
            case COSINE -> "COSINE";
            case IP -> "IP";
            case L2 -> "L2";
        };
    }

    private IndexParam.MetricType toMilvusMetricParam(DistanceMetric metric) {
        return switch (metric) {
            case COSINE -> IndexParam.MetricType.COSINE;
            case IP -> IndexParam.MetricType.IP;
            case L2 -> IndexParam.MetricType.L2;
        };
    }

    private IndexParam toMilvusIndexParam(String indexType, DistanceMetric metric, int dimension) {
        IndexParam.IndexType type = toMilvusIndexType(indexType);
        Map<String, Object> extraParams = new HashMap<>();
        if (type == IndexParam.IndexType.HNSW) {
            extraParams.put("M", 16);
            extraParams.put("efConstruction", 200);
        } else if (type == IndexParam.IndexType.IVF_FLAT
                || type == IndexParam.IndexType.IVF_SQ8
                || type == IndexParam.IndexType.IVF_PQ) {
            extraParams.put("nlist", 128);
        }
        return IndexParam.builder()
                .fieldName(EMBEDDING_FIELD)
                .indexType(type)
                .metricType(toMilvusMetricParam(metric))
                .extraParams(extraParams)
                .build();
    }

    private IndexParam.IndexType toMilvusIndexType(String indexType) {
        if (!StringUtils.hasText(indexType)) {
            return IndexParam.IndexType.HNSW;
        }
        try {
            return IndexParam.IndexType.valueOf(indexType.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return IndexParam.IndexType.HNSW;
        }
    }

    private String toString(Object value) {
        return value != null ? value.toString() : null;
    }

    private int parseInt(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number n) {
            return n.intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseMetadata(Object value) {
        if (value == null) {
            return new HashMap<>();
        }
        if (value instanceof Map) {
            return (Map<String, Object>) value;
        }
        return gson.fromJson(value.toString(), Map.class);
    }
}
