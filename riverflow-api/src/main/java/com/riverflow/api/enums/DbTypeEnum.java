package com.riverflow.api.enums;

import lombok.Getter;

/**
 * 数据库类型枚举
 */
@Getter
public enum DbTypeEnum {

    MYSQL("mysql", "com.mysql.cj.jdbc.Driver"),
    ORACLE("oracle", "oracle.jdbc.driver.OracleDriver"),
    POSTGRESQL("postgresql", "org.postgresql.Driver"),
    SQLSERVER("sqlserver", "com.microsoft.sqlserver.jdbc.SQLServerDriver"),
    DM("dm", "dm.jdbc.driver.DmDriver"),
    OTHER("other", null);

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
        return OTHER;
    }

    /**
     * 是否为内置驱动类型（无需用户上传 JAR）
     */
    public static boolean isBuiltIn(String code) {
        return MYSQL.getCode().equalsIgnoreCase(code)
                || ORACLE.getCode().equalsIgnoreCase(code)
                || POSTGRESQL.getCode().equalsIgnoreCase(code)
                || SQLSERVER.getCode().equalsIgnoreCase(code)
                || DM.getCode().equalsIgnoreCase(code);
    }
}
