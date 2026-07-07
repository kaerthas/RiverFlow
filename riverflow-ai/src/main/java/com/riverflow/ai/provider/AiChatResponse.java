package com.riverflow.ai.provider;

import lombok.Builder;
import lombok.Data;

/**
 * LLM 聊天响应
 */
@Data
@Builder
public class AiChatResponse {

    /**
     * LLM 返回的文本内容
     */
    private String content;

    /**
     * 实际使用的模型
     */
    private String model;

    /**
     * Prompt token 数
     */
    private Integer promptTokens;

    /**
     * 生成 token 数
     */
    private Integer completionTokens;

    /**
     * 总 token 数
     */
    private Integer totalTokens;

    /**
     * 响应耗时（毫秒）
     */
    private Long responseTimeMs;

    /**
     * provider 名称
     */
    private String provider;
}
