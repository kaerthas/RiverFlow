package com.riverflow.admin.infra.datascope;

/**
 * 数据权限范围常量
 */
public final class DataScopeScope {

    private DataScopeScope() {
    }

    /** 全部数据 */
    public static final int ALL = 1;

    /** 本部门数据 */
    public static final int DEPT_ONLY = 2;

    /** 本部门及以下数据 */
    public static final int DEPT_AND_CHILD = 3;

    /** 仅本人数据 */
    public static final int SELF_ONLY = 4;

    /** 自定义部门数据 */
    public static final int CUSTOM = 5;
}
