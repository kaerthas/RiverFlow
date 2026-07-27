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
     * 可用 API 列表（带元数据，辅助 AI 绑定真实接口及参数映射）
     */
    private List<ApiInfo> availableApis;

    /**
     * 可用数据库数据源（带元数据）
     */
    private List<DbSourceInfo> availableDbSources;

    /**
     * 已加载的节点插件列表（左侧插件节点面板）
     */
    private List<NodePluginInfo> availableNodePlugins;

    /**
     * 额外上下文
     */
    private Map<String, Object> extraContext;

    /**
     * 是否跳过沙箱模拟执行（默认不跳过）
     */
    private boolean skipSimulation;

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

    /**
     * 可用 API 元数据
     */
    @Data
    public static class ApiInfo {
        private String apiCode;
        private String apiName;
        private String apiType;
        private String pluginType;
        private String method;
        private String url;
        private String contentType;
        private String authType;
        private List<ParamInfo> headers;
        private List<ParamInfo> queryParams;
        private List<ParamInfo> bodyParams;
        private List<ParamInfo> responseParams;
    }

    /**
     * 接口参数元数据
     */
    @Data
    public static class ParamInfo {
        private String paramKey;
        private String paramName;
        private String paramType;
        private String dataType;
        private Integer required;
        private String defaultValue;
    }

    /**
     * 可用数据源元数据
     */
    @Data
    public static class DbSourceInfo {
        private String dsCode;
        private String dsName;
        private String dbType;
    }

    /**
     * 节点插件元数据
     */
    @Data
    public static class NodePluginInfo {
        private String nodeType;
        private String nodeName;
        private String description;
        private String outputSchema;
    }
}

