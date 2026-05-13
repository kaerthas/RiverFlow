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
}
