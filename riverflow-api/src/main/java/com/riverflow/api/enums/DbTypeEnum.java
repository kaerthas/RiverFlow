package com.riverflow.api.enums;

import lombok.Getter;

/**
 * 数据库类型枚举
 */
@Getter
public enum DbTypeEnum {

    MYSQL("mysql", "com.mysql.cj.jdbc.Driver"),
    ORACLE("oracle", "oracle.jdbc.driver.OracleDriver"),
    SQLSERVER("sqlserver", "com.microsoft.sqlserver.jdbc.SQLServerDriver"),
    POSTGRESQL("postgresql", "org.postgresql.Driver");

    private final String code;
    private final String driverClass;

    DbTypeEnum(String code, String driverClass) {
        this.code = code;
        this.driverClass = driverClass;
    }

    public static DbTypeEnum fromCode(String code) {
        for (DbTypeEnum type : values()) {
            if (type.getCode().equalsIgnoreCase(code)) {
                return type;
            }
        }
        return MYSQL;
    }
}
