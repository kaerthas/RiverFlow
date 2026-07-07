package com.riverflow.ai.dto;

import com.alibaba.fastjson2.JSONObject;
import lombok.Data;

import java.util.List;

/**
 * AI 生成流程响应
 */
@Data
public class AiGenerateFlowResponse {

    private String flowName;
    private String description;
    private String triggerType;
    private String triggerConfig;
    private String executionMode;
    private String thinking;
    private JSONObject graphJson;
    private List<FlowNodeDraft> nodes;
    private List<FlowEdgeDraft> edges;

    @Data
    public static class FlowNodeDraft {
        private String nodeId;
        private String nodeType;
        private String configJson;
    }

    @Data
    public static class FlowEdgeDraft {
        private String sourceNode;
        private String targetNode;
        private String conditionType;
        private String conditionExpression;
        private Integer priority;
    }
}
