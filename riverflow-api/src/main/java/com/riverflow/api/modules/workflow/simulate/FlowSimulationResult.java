package com.riverflow.api.modules.workflow.simulate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 流程模拟执行结果
 *
 * <p>记录沙箱模拟执行的完整路径、每个节点输入/输出、上下文变化及错误信息。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlowSimulationResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 是否模拟成功（无执行期错误且到达结束节点）
     */
    private boolean success;

    /**
     * 总体错误信息
     */
    private String errorMsg;

    /**
     * 模拟执行步骤
     */
    @Builder.Default
    private List<SimulationStep> steps = new ArrayList<>();

    /**
     * 最终上下文快照（key-value）
     */
    private Map<String, Object> finalContext;

    /**
     * 是否到达结束节点
     */
    private boolean reachedEnd;

    public void addStep(SimulationStep step) {
        if (this.steps == null) {
            this.steps = new ArrayList<>();
        }
        this.steps.add(step);
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimulationStep implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * 节点 ID
         */
        private String nodeId;

        /**
         * 节点名称
         */
        private String nodeName;

        /**
         * 节点类型
         */
        private String nodeType;

        /**
         * 执行前上下文快照
         */
        private Map<String, Object> inputContext;

        /**
         * 执行后上下文快照
         */
        private Map<String, Object> outputContext;

        /**
         * 节点是否执行成功
         */
        private boolean success;

        /**
         * 节点执行错误信息
         */
        private String errorMsg;

        /**
         * 节点返回结果数据
         */
        private Object resultData;

        /**
         * 执行耗时（毫秒）
         */
        private Long executeTimeMillis;
    }
}
