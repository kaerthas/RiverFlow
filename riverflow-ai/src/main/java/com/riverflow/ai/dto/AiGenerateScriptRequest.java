package com.riverflow.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * AI 生成 Groovy 脚本请求
 */
@Data
public class AiGenerateScriptRequest {

    /**
     * 自然语言描述
     */
    @NotBlank(message = "脚本需求描述不能为空")
    private String userPrompt;

    /**
     * 可用上下文变量
     */
    private List<String> contextVariables;

    /**
     * 期望输出变量名
     */
    private String expectedOutput;

    /**
     * 指定 LLM provider，为空时使用默认 provider
     */
    private String provider;

    /**
     * 指定模型，为空时使用 provider 默认模型
     */
    private String model;
}
