package com.riverflow.ai.knowledge.vector;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 内存向量存储（仅用于测试和开发）
 */
@Component
public class InMemoryVectorStore implements VectorStoreProvider {

    public static final String TYPE = "memory";

    private final Map<String, Map<String, VectorDocument>> store = new ConcurrentHashMap<>();

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public void createCollection(String collection, int dimension, DistanceMetric metric) {
        store.putIfAbsent(collection, new ConcurrentHashMap<>());
    }

    @Override
    public void upsert(String collection, List<VectorDocument> documents) {
        Map<String, VectorDocument> col = store.computeIfAbsent(collection, k -> new ConcurrentHashMap<>());
        for (VectorDocument doc : documents) {
            col.put(doc.getId(), doc);
        }
    }

    @Override
    public List<VectorDocument> search(String collection, float[] vector, int topK, double minScore) {
        Map<String, VectorDocument> col = store.getOrDefault(collection, Collections.emptyMap());
        if (col.isEmpty()) {
            return Collections.emptyList();
        }
        List<VectorDocument> results = new ArrayList<>();
        for (VectorDocument doc : col.values()) {
            if (doc.getEmbedding() == null || doc.getEmbedding().length != vector.length) {
                continue;
            }
            double score = cosineSimilarity(vector, doc.getEmbedding());
            if (score >= minScore) {
                VectorDocument copy = copy(doc);
                copy.setScore(score);
                results.add(copy);
            }
        }
        results.sort(Comparator.comparing(VectorDocument::getScore).reversed());
        return results.stream().limit(topK).collect(Collectors.toList());
    }

    @Override
    public void deleteByIds(String collection, List<String> ids) {
        Map<String, VectorDocument> col = store.get(collection);
        if (col == null) {
            return;
        }
        for (String id : ids) {
            col.remove(id);
        }
    }

    @Override
    public void dropCollection(String collection) {
        store.remove(collection);
    }

    @Override
    public boolean collectionExists(String collection) {
        return store.containsKey(collection);
    }

    private double cosineSimilarity(float[] a, float[] b) {
        double dot = 0;
        double normA = 0;
        double normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        if (normA == 0 || normB == 0) {
            return 0;
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private VectorDocument copy(VectorDocument source) {
        VectorDocument copy = new VectorDocument();
        copy.setId(source.getId());
        copy.setCollection(source.getCollection());
        copy.setDocId(source.getDocId());
        copy.setChunkIndex(source.getChunkIndex());
        copy.setContent(source.getContent());
        copy.setEmbedding(source.getEmbedding());
        copy.setMetadata(source.getMetadata() != null ? new HashMap<>(source.getMetadata()) : null);
        return copy;
    }
}
