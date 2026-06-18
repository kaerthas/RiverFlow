package com.riverflow.admin.infra.openapi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 开放接口认证结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpenApiAuthResult {

    private boolean passed;
    private int code;
    private String message;

    public static OpenApiAuthResult ok() {
        return new OpenApiAuthResult(true, 200, "OK");
    }

    public static OpenApiAuthResult fail(int code, String message) {
        return new OpenApiAuthResult(false, code, message);
    }
}
