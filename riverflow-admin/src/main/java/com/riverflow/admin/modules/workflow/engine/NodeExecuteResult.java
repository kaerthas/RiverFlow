package com.riverflow.admin.modules.workflow.engine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 节点执行结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NodeExecuteResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 结果数据（会写入上下文）
     */
    private Object data;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 是否等待（定时节点使用）
     */
    private boolean waiting;

    /**
     * 等待到下次执行的时间戳（毫秒）
     */
    private Long nextExecuteTime;

    // ==================== 循环节点扩展字段 ====================

    /**
     * 指定下一入口节点（用于循环跳转）
     */
    private String nextEntryNodeId;

    /**
     * 是否退出循环
     */
    private boolean exitLoop;

    /**
     * 是否是循环控制节点产生的跳转
     */
    private boolean loopControl;

    /**
     * 循环节点 ID（循环控制结果携带，用于任务标记）
     */
    private String loopNodeId;

    /**
     * 循环迭代下标（循环控制结果携带）
     */
    private Integer iterationIndex;

    /**
     * 是否终止当前执行链（用于异步并行 foreach 调度后不再继续流转）
     */
    private boolean terminateChain;

    public static NodeExecuteResult success() {
        return NodeExecuteResult.builder().success(true).build();
    }

    public static NodeExecuteResult success(Object data) {
        return NodeExecuteResult.builder().success(true).data(data).build();
    }

    public static NodeExecuteResult fail(String errorMsg) {
        return NodeExecuteResult.builder().success(false).errorMsg(errorMsg).build();
    }

    public static NodeExecuteResult waiting(Long nextExecuteTime) {
        return NodeExecuteResult.builder().success(true).waiting(true).nextExecuteTime(nextExecuteTime).build();
    }

    public NodeExecuteResult withNextEntryNode(String nextEntryNodeId) {
        this.nextEntryNodeId = nextEntryNodeId;
        this.loopControl = true;
        return this;
    }

    public NodeExecuteResult withNextEntryNode(String nextEntryNodeId, String loopNodeId, Integer iterationIndex) {
        this.nextEntryNodeId = nextEntryNodeId;
        this.loopNodeId = loopNodeId;
        this.iterationIndex = iterationIndex;
        this.loopControl = true;
        return this;
    }

    public NodeExecuteResult withExitLoop(boolean exitLoop) {
        this.exitLoop = exitLoop;
        this.loopControl = true;
        return this;
    }

    public NodeExecuteResult withTerminateChain(boolean terminateChain) {
        this.terminateChain = terminateChain;
        return this;
    }
}
