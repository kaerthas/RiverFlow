package com.riverflow.ai.dto;

import lombok.Data;

/**
 * LLM Provider 脱敏信息
 *
 * <p>用于向前端返回可用的 provider 列表，不包含 apiKey、baseUrl 等敏感配置。
 */
@Data
public class AiProviderInfo {

    /**
     * provider 唯一标识
     */
    private String name;

    /**
     * provider 类型：openai / ollama / zhipu / qwen 等
     */
    private String type;

    /**
     * 默认模型名称
     */
    private String defaultModel;
}
