package com.riverflow.ai.knowledge.dto;

import lombok.Data;

/**
 * Embedding 连接测试请求
 */
@Data
public class EmbeddingTestRequest {

    /**
     * Embedding 类型：openai / ollama / qwen / zhipu / memory
     */
    private String type;

    /**
     * 基础 URL
     */
    private String baseUrl;

    /**
     * API Key
     */
    private String apiKey;

    /**
     * 模型名称
     */
    private String model;

    /**
     * 向量维度
     */
    private Integer dimension;

    /**
     * 调用超时（毫秒）
     */
    private Integer timeout;

    /**
     * 测试文本，为空时使用默认文本
     */
    private String text;
}
