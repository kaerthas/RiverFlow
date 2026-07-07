package com.riverflow.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * AI 接口文档解析请求
 */
@Data
public class AiParseApiDocRequest {

    /**
     * 接口文档内容：OpenAPI/Swagger JSON 或 Markdown/HTML 描述
     */
    @NotBlank(message = "接口文档内容不能为空")
    private String docContent;

    /**
     * 解析选项：extractParams / extractResponses / generateMapping
     */
    private List<String> options;

    /**
     * 指定 LLM provider，为空时使用默认 provider
     */
    private String provider;

    /**
     * 指定模型，为空时使用 provider 默认模型
     */
    private String model;
}
