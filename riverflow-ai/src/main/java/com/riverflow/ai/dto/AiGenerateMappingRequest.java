package com.riverflow.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * AI 推荐数据映射请求
 */
@Data
public class AiGenerateMappingRequest {

    /**
     * 映射方向：input / output
     */
    @NotBlank(message = "映射方向不能为空")
    private String direction;

    /**
     * 用户补充说明
     */
    private String userPrompt;

    /**
     * API 参数树
     */
    @NotNull(message = "API 参数树不能为空")
    private List<ApiParamNode> apiParams;

    /**
     * 流程上下文变量
     */
    private List<String> contextVariables;

    /**
     * 示例响应
     */
    private Map<String, Object> sampleResponse;

    /**
     * 额外上下文：前端用户可传入的补充说明、约束条件等
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

    @Data
    public static class ApiParamNode {
        private String paramType;
        private String paramKey;
        private String paramName;
        private String dataType;
        private List<ApiParamNode> children;
    }
}
