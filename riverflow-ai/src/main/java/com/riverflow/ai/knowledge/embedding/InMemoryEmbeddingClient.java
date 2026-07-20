package com.riverflow.ai.knowledge.embedding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 内存 Embedding 客户端（仅用于测试）
 *
 * <p>不需要外部 Embedding 服务，直接返回固定随机向量。
 * 与 {@link com.riverflow.ai.knowledge.vector.InMemoryVectorStore} 配合使用，
 * 可在无 Milvus / 无 API Key 环境下快速验证知识库流程。</p>
 */
public class InMemoryEmbeddingClient implements EmbeddingClient {

    public static final String TYPE = "memory";

    private final int dimension;
    private final Random random;
    // 用相同的文本会返回相同的向量，保证检索可重复
    private final ConcurrentMap<String, float[]> cache;

    public InMemoryEmbeddingClient(int dimension) {
        this.dimension = dimension;
        this.random = new Random(42);
        this.cache = new ConcurrentHashMap<>();
    }

    @Override
    public List<float[]> embed(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return Collections.emptyList();
        }
        List<float[]> result = new ArrayList<>(texts.size());
        for (String text : texts) {
            if (text == null || text.isBlank()) {
                result.add(new float[dimension]);
                continue;
            }
            result.add(cache.computeIfAbsent(text.trim(), this::generateVector));
        }
        return result;
    }

    private float[] generateVector(String text) {
        float[] vector = new float[dimension];
        // 基于文本 hash 生成确定性的随机种子，保证相同文本向量相同
        Random r = new Random(text.hashCode());
        for (int i = 0; i < dimension; i++) {
            vector[i] = r.nextFloat() * 2 - 1;
        }
        normalize(vector);
        return vector;
    }

    private void normalize(float[] vector) {
        double sum = 0;
        for (float v : vector) {
            sum += v * v;
        }
        if (sum == 0) {
            return;
        }
        double norm = Math.sqrt(sum);
        for (int i = 0; i < vector.length; i++) {
            vector[i] = (float) (vector[i] / norm);
        }
    }

    @Override
    public int dimension() {
        return dimension;
    }

    @Override
    public String name() {
        return TYPE;
    }
}
