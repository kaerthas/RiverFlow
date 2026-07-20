package com.riverflow.ai.knowledge.dto;

import lombok.Data;

/**
 * Embedding 连接测试响应
 */
@Data
public class EmbeddingTestResponse {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 提示信息
     */
    private String message;

    /**
     * 样本向量维度
     */
    private Integer sampleDimension;

    /**
     * 耗时（毫秒）
     */
    private Long elapsedMs;

    /**
     * 测试使用的模型
     */
    private String model;

    public static EmbeddingTestResponse success(String message, Integer dimension, Long elapsedMs, String model) {
        EmbeddingTestResponse response = new EmbeddingTestResponse();
        response.setSuccess(true);
        response.setMessage(message);
        response.setSampleDimension(dimension);
        response.setElapsedMs(elapsedMs);
        response.setModel(model);
        return response;
    }

    public static EmbeddingTestResponse fail(String message) {
        EmbeddingTestResponse response = new EmbeddingTestResponse();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }
}
