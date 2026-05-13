package com.riverflow.api.entity;

import com.baomidou.mybatisplus.annotation.TableName;
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
     * 请求方式：GET/POST/PUT/DELETE
     */
    private String method;

    /**
     * 请求地址
     */
    private String url;

    /**
     * 请求体类型
     */
    private String contentType;

    /**
     * 认证方式：none/basic/token/oauth2
     */
    private String authType;

    /**
     * SQL类型时绑定的数据源ID
     */
    private Long dsId;

    /**
     * 脚本类型时绑定的脚本ID
     */
    private Long scriptId;

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
     * 状态：0-草稿，1-已发布，2-下线
     */
    private Integer status;
}
