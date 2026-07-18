package com.riverflow.ai.knowledge.embedding;

import java.util.List;

/**
 * Embedding 客户端抽象
 *
 * <p>将文本转换为向量。支持云端 Embedding API（OpenAI / Ollama / 通义千问）以及后续本地模型扩展。</p>
 */
public interface EmbeddingClient {

    /**
     * 批量向量化
     *
     * @param texts 待向量化的文本列表
     * @return 向量列表，顺序与输入一致
     */
    List<float[]> embed(List<String> texts);

    /**
     * 单条向量化
     */
    default float[] embed(String text) {
        List<float[]> results = embed(List.of(text));
        return results.isEmpty() ? new float[0] : results.get(0);
    }

    /**
     * 向量维度
     */
    int dimension();

    /**
     * 客户端名称
     */
    String name();
}
