package com.riverflow.admin.infra.openapi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SQL 安全检查返回结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SqlCheckResult {

    private boolean passed;
    private String message;

    public static SqlCheckResult ok() {
        return new SqlCheckResult(true, "OK");
    }

    public static SqlCheckResult fail(String message) {
        return new SqlCheckResult(false, message);
    }
}
