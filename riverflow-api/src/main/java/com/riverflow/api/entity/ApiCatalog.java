package com.riverflow.api.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.riverflow.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 接口目录
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wf_api_catalog")
public class ApiCatalog extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 所属应用ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long appId;

    /**
     * 接口编码
     */
    private String apiCode;

    /**
     * 接口名称
     */
    private String apiName;

    /**
     * 接口类型：proxy-代理 sql-SQL服务 data-数据服务 script-脚本服务
     */
    private String apiType;

    /**
     * 请求方式：GET/POST/PUT/DELETE（原始目标接口的请求方式）
     */
    private String method;

    /**
     * 请求地址（原始目标接口地址，proxy类型为下游URL，sql类型为SQL语句）
     */
    private String url;

    /**
     * 代理后暴露路径，如 /user/list
     */
    private String openPath;

    /**
     * 代理后请求方式：GET/POST/PUT/DELETE
     */
    private String openMethod;

    /**
     * 请求体类型
     */
    private String contentType;

    /**
     * 认证方式：none/basic/token/sign/oauth2
     * sign 表示 AppKey + AppSecret 请求签名
     */
    private String authType;

    /**
     * 调用方 IP 白名单，多个用逗号分隔，支持 CIDR，如 10.0.0.0/24,192.168.1.10
     */
    private String allowedIps;

    /**
     * SQL类型时绑定的数据源ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long dsId;

    /**
     * 脚本类型时绑定的脚本ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long scriptId;

    /**
     * 插件类型标识，api_type=plugin 时生效
     */
    private String pluginType;

    /**
     * 超时毫秒
     */
    private Integer timeout;

    /**
     * 重试次数
     */
    private Integer retryTimes;

    /**
     * 是否启用代理
     */
    private Integer proxyEnabled;

    /**
     * 代理主机
     */
    private String proxyHost;

    /**
     * 代理端口
     */
    private Integer proxyPort;

    /**
     * 是否启用流程触发：0-否 1-是
     */
    private Integer triggerEnabled;

    /**
     * 执行成功后触发的流程定义ID（兼容旧数据）
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long triggerFlowId;

    /**
     * 触发流程编码（绑定编码，自动取最新发布版本）
     */
    private String triggerFlowCode;

    /**
     * 从请求参数中提取业务主键的字段名
     */
    private String triggerBizKeyField;

    /**
     * 状态：0-草稿，1-已发布，2-下线
     */
    private Integer status;

    /**
     * 业务成功状态码，多个用逗号分隔，如 200,0,1
     * 默认 200
     */
    private String successCode;
}
