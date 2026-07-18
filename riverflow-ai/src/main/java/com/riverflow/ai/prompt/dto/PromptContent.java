package com.riverflow.ai.prompt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Prompt 内容封装
 *
 * <p>包含系统 Prompt、用户 Prompt 模板、Few-shot 示例、输出 Schema。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptContent {

    /**
     * 场景
     */
    private String scene;

    /**
     * 模型
     */
    private String model;

    /**
     * 版本
     */
    private String version;

    /**
     * 系统 Prompt
     */
    private String systemPrompt;

    /**
     * 用户 Prompt 模板
     */
    private String template;

    /**
     * Few-shot 示例 JSON
     */
    private String examples;

    /**
     * 输出 JSON Schema
     */
    private String outputSchema;

    /**
     * 来源：database / classpath
     */
    private String source;
}
