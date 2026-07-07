package com.riverflow.ai.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

/**
 * AI 服务配置属性
 *
 * <p>示例配置：
 * <pre>
 * riverflow:
 *   ai:
 *     enabled: true
 *     audit-enabled: true
 *     default-provider: ollama
 *     timeout: 30000
 *     retry: 1
 *     providers:
 *       - name: ollama
 *         type: ollama
 *         base-url: http://localhost:11434
 *         default-model: qwen2.5:14b
 *       - name: openai
 *         type: openai
 *         base-url: https://api.openai.com/v1
 *         api-key: ${OPENAI_API_KEY}
 *         default-model: gpt-4o-mini
 * </pre>
 */
@Data
@Component
@Validated
@ConfigurationProperties(prefix = "riverflow.ai")
public class AiProperties {

    /**
     * 是否启用 AI 服务
     */
    private boolean enabled = true;

    /**
     * 是否记录调用审计日志
     */
    private boolean auditEnabled = true;

    /**
     * 是否对输入输出进行敏感信息脱敏
     */
    private boolean sensitiveMaskEnabled = true;

    /**
     * 默认使用的 provider 名称
     */
    private String defaultProvider;

    /**
     * LLM HTTP 调用超时（毫秒）
     */
    private int timeout = 30000;

    /**
     * 失败重试次数（不含首次）
     */
    private int retry = 1;

    /**
     * Provider 列表（配置文件中的静态配置，数据库无可用配置时作为兜底）
     */
    private List<Provider> providers = new ArrayList<>();

    @Data
    public static class Provider {

        /**
         * provider 唯一标识
         */
        @NotBlank
        private String name;

        /**
         * provider 类型：openai / ollama / zhipu / qwen 等
         */
        @NotBlank
        private String type;

        /**
         * 基础 URL
         */
        @NotBlank
        private String baseUrl;

        /**
         * API Key（部分 provider 不需要）
         */
        private String apiKey;

        /**
         * 默认模型
         */
        @NotBlank
        private String defaultModel;

        /**
         * 默认温度
         */
        private Float temperature = 0.2f;

        /**
         * 默认最大 token
         */
        private Integer maxTokens = 4096;

        /**
         * 上下文窗口大小（仅 Ollama 有效，对应 num_ctx）
         */
        private Integer contextSize = 8192;
    }
}
