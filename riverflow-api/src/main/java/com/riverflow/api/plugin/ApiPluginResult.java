package com.riverflow.api.plugin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 接口插件执行结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiPluginResult {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 响应数据
     */
    private Object data;

    /**
     * 错误信息
     */
    private String errorMsg;

    public static ApiPluginResult ok(Object data) {
        ApiPluginResult result = new ApiPluginResult();
        result.success = true;
        result.data = data;
        return result;
    }

    public static ApiPluginResult ok() {
        return ok(null);
    }

    public static ApiPluginResult fail(String errorMsg) {
        ApiPluginResult result = new ApiPluginResult();
        result.success = false;
        result.errorMsg = errorMsg;
        return result;
    }
}
