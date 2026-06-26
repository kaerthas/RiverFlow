package com.riverflow.admin.modules.workflow.loop;

import com.riverflow.admin.modules.workflow.context.FlowContext;
import com.riverflow.api.entity.FlowTask;
import com.riverflow.api.enums.FlowTaskTypeEnum;

import java.util.Map;

/**
 * 循环任务字段填充工具
 * 统一处理 FlowTask 与循环上下文相关的字段写入。
 */
public class LoopTaskHelper {

    private LoopTaskHelper() {
    }

    /**
     * 根据当前上下文作用域填充循环相关字段
     */
    public static void fillLoopFields(FlowTask task, FlowContext context) {
        if (task == null || context == null || !context.inScope()) {
            return;
        }
        task.setIsLoopInternal(1);
        Map<String, Object> loopFrame = context.getCurrentLoopFrame();
        if (loopFrame != null) {
            Object loopNodeId = loopFrame.get("loopNodeId");
            if (loopNodeId != null) {
                task.setLoopNodeId(loopNodeId.toString());
            }
            Object iterationIndex = loopFrame.get("iterationIndex");
            if (iterationIndex instanceof Number) {
                task.setIterationIndex(((Number) iterationIndex).intValue());
            }
        }
    }

    /**
     * 安全设置任务类型，空值时默认为 NODE
     */
    public static void setTaskType(FlowTask task, FlowTaskTypeEnum taskType) {
        if (task == null) {
            return;
        }
        if (taskType == null) {
            taskType = FlowTaskTypeEnum.NODE;
        }
        task.setTaskType(taskType.getCode());
    }
}
