package com.riverflow.admin.infra.datascope;

import java.lang.annotation.*;

/**
 * 数据权限注解
 * 标注在 Service/Mapper 方法上，自动追加数据范围过滤条件
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {

    /**
     * 数据权限范围
     * <ul>
     *     <li>1 - 全部数据</li>
     *     <li>2 - 本部门数据</li>
     *     <li>3 - 本部门及以下数据</li>
     *     <li>4 - 仅本人数据</li>
     *     <li>5 - 自定义部门数据</li>
     * </ul>
     */
    int scope() default DataScopeScope.DEPT_AND_CHILD;

    /**
     * 自定义部门 ID 集合，scope = CUSTOM 时生效
     */
    String[] customDeptIds() default {};

    /**
     * 数据权限字段名，默认 dept_id
     */
    String deptColumn() default "dept_id";

    /**
     * 创建人字段名，默认 create_by
     */
    String userColumn() default "create_by";
}
