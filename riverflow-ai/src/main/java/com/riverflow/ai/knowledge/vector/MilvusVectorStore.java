package com.riverflow.ai.knowledge.vector;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.riverflow.ai.config.AiProperties;
import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.collection.request.DropCollectionReq;
import io.milvus.v2.service.collection.request.HasCollectionReq;
import io.milvus.v2.service.vector.request.DeleteReq;
import io.milvus.v2.service.vector.request.SearchReq;
import io.milvus.v2.service.vector.request.UpsertReq;
import io.milvus.v2.service.vector.request.data.FloatVec;
import io.milvus.v2.service.vector.response.SearchResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Milvus 向量存储实现
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
    private MilvusClientV2 client;
    private final Gson gson = new Gson();

    @Autowired
    public MilvusVectorStore(AiProperties aiProperties) {
        this.aiProperties = aiProperties;
    }

    @PostConstruct
    public void init() {
        if (!TYPE.equalsIgnoreCase(aiProperties.getKnowledge().getVectorStore().getType())) {
            return;
        }
        AiProperties.MilvusConfig config = aiProperties.getKnowledge().getVectorStore().getMilvus();
        ConnectConfig.ConnectConfigBuilder builder = ConnectConfig.builder()
                .uri(buildUri(config));
        if (config.getToken() != null && !config.getToken().isBlank()) {
            builder.token(config.getToken());
        }
        this.client = new MilvusClientV2(builder.build());
        log.info("Milvus 客户端初始化完成: uri={}", buildUri(config));
    }

    @PreDestroy
    public void destroy() {
        if (client != null) {
            try {
                client.close();
            } catch (Exception e) {
                log.warn("关闭 Milvus 客户端失败", e);
            }
        }
    }

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public void createCollection(String collection, int dimension, DistanceMetric metric) {
        ensureClient();
        if (collectionExists(collection)) {
            return;
        }
        String indexType = aiProperties.getKnowledge().getVectorStore().getMilvus().getIndexType();
        CreateCollectionReq req = CreateCollectionReq.builder()
                .collectionName(collection)
                .dimension(dimension)
                .primaryFieldName(ID_FIELD)
                .vectorFieldName(EMBEDDING_FIELD)
                .metricType(toMilvusMetric(metric))
                .autoID(false)
                .build();
        client.createCollection(req);
        log.info("Milvus 集合创建完成: collection={}, dimension={}, metric={}, index={}",
                collection, dimension, metric, indexType);
    }

    @Override
    public void upsert(String collection, List<VectorDocument> documents) {
        ensureClient();
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
        ensureClient();
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
        ensureClient();
        client.delete(DeleteReq.builder()
                .collectionName(collection)
                .ids(new ArrayList<>(ids))
                .build());
    }

    @Override
    public void dropCollection(String collection) {
        ensureClient();
        if (!collectionExists(collection)) {
            return;
        }
        client.dropCollection(DropCollectionReq.builder()
                .collectionName(collection)
                .build());
    }

    @Override
    public boolean collectionExists(String collection) {
        ensureClient();
        return client.hasCollection(HasCollectionReq.builder()
                .collectionName(collection)
                .build());
    }

    private void ensureClient() {
        if (client == null) {
            throw new IllegalStateException("Milvus 客户端未初始化，请检查配置 riverflow.ai.knowledge.vector-store.type");
        }
    }

    private String buildUri(AiProperties.MilvusConfig config) {
        String protocol = config.isSecure() ? "https" : "http";
        return protocol + "://" + config.getHost() + ":" + config.getPort();
    }

    private JsonObject toMilvusRow(VectorDocument doc) {
        JsonObject row = new JsonObject();
        row.addProperty(ID_FIELD, doc.getId());
        row.addProperty(DOC_ID_FIELD, doc.getDocId());
        row.addProperty(CHUNK_INDEX_FIELD, doc.getChunkIndex());
        row.addProperty(CONTENT_FIELD, doc.getContent());
        row.add(METADATA_FIELD, gson.toJsonTree(doc.getMetadata() != null ? doc.getMetadata() : new HashMap<>()));
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
