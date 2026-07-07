package com.riverflow.ai.dto;

import lombok.Data;

import java.util.List;

/**
 * AI 接口文档解析响应
 *
 * <p>输出结构可直接用于 RiverFlow 接口注册（API 目录）。
 */
@Data
public class AiParseApiDocResponse {

    /**
     * API 名称
     */
    private String apiName;

    /**
     * API 编码（建议英文/下划线）
     */
    private String apiCode;

    /**
     * HTTP 方法
     */
    private String method;

    /**
     * 请求路径
     */
    private String path;

    /**
     * 接口摘要
     */
    private String summary;

    /**
     * 接口详细描述
     */
    private String description;

    /**
     * 请求参数列表
     */
    private List<ApiParameter> parameters;

    /**
     * 请求体定义
     */
    private ApiBody requestBody;

    /**
     * 响应列表
     */
    private List<ApiResponse> responses;

    /**
     * 推荐映射（当 options 包含 generateMapping 时返回）
     */
    private List<RecommendedMapping> recommendedMappings;

    @Data
    public static class ApiParameter {
        private String name;
        private String in;
        private Boolean required;
        private String dataType;
        private String description;
    }

    @Data
    public static class ApiBody {
        private String contentType;
        private String schemaJson;
        private List<ApiParameter> fields;
    }

    @Data
    public static class ApiResponse {
        private String status;
        private String description;
        private String contentType;
        private String schemaJson;
        private List<ApiParameter> fields;
    }

    @Data
    public static class RecommendedMapping {
        private String source;
        private String target;
        private String type;
        private String description;
    }
}
