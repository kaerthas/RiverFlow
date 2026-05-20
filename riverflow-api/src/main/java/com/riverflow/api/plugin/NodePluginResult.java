package com.riverflow.api.plugin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 插件执行结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NodePluginResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 结果数据（会写入流程上下文）
     */
    private Object data;

    /**
     * 错误信息
     */
    private String errorMsg;

    /**
     * 是否等待（用于异步操作）
     */
    private boolean waiting;

    /**
     * 等待到下次执行的时间戳（毫秒）
     */
    private Long nextExecuteTime;

    public static NodePluginResult success() {
        return NodePluginResult.builder().success(true).build();
    }

    public static NodePluginResult success(Object data) {
        return NodePluginResult.builder().success(true).data(data).build();
    }

    public static NodePluginResult fail(String errorMsg) {
        return NodePluginResult.builder().success(false).errorMsg(errorMsg).build();
    }

    public static NodePluginResult waiting(Long nextExecuteTime) {
        return NodePluginResult.builder().success(true).waiting(true).nextExecuteTime(nextExecuteTime).build();
    }
}
