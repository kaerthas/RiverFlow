package com.riverflow.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * AI 生成流程请求
 */
@Data
public class AiGenerateFlowRequest {

    /**
     * 自然语言描述
     */
    @NotBlank(message = "流程描述不能为空")
    private String userPrompt;

    /**
     * 可用 API 列表
     */
    private List<String> availableApis;

    /**
     * 可用数据库数据源
     */
    private List<String> availableDbSources;

    /**
     * 额外上下文
     */
    private Map<String, Object> extraContext;

    /**
     * 指定 LLM provider，为空时使用默认 provider
     */
    private String provider;

    /**
     * 指定模型，为空时使用 provider 默认模型
     */
    private String model;

    /**
     * 指定 Prompt 版本，为空时使用默认版本 v1
     */
    private String promptVersion;
}
