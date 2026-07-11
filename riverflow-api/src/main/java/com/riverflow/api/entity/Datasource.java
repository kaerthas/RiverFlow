package com.riverflow.api.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.riverflow.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 动态数据源配置
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wf_datasource")
public class Datasource extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 数据源编码
     */
    private String dsCode;

    /**
     * 数据源名称
     */
    private String dsName;

    /**
     * 数据库类型：mysql/oracle/sqlserver/postgresql
     */
    private String dbType;

    /**
     * 连接URL
     */
    private String url;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码（Jasypt加密）
     */
    private String password;

    /**
     * 驱动类名
     */
    private String driverClass;

    /**
     * 驱动JAR包路径（自定义驱动时使用）
     */
    private String driverJarPath;

    /**
     * 部门ID（数据权限）
     */
    private Long deptId;

    /**
     * 状态：0-停用，1-启用
     */
    private Integer status;
}
