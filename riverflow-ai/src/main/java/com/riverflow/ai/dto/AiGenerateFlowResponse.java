package com.riverflow.ai.dto;

import com.alibaba.fastjson2.JSONObject;
import com.riverflow.api.modules.workflow.simulate.FlowSimulationResult;
import com.riverflow.api.modules.workflow.validate.FlowValidationResult;
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

    /**
     * 流程结构/语法/业务校验结果
     */
    private FlowValidationResult validationResult;

    /**
     * 沙箱模拟执行结果
     */
    private FlowSimulationResult simulationResult;

    /**
     * 是否需要人工复核（校验或模拟不通过时建议复核）
     */
    private boolean reviewRequired;

    /**
     * 修复轮次（0 表示未触发修复）
     */
    private int fixRounds;

    /**
     * 修复历史记录
     */
    private List<String> fixHistory;

    /**
     * 是否已完全修复（校验与模拟均通过）
     */
    private boolean fullyRepaired;

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
