package com.riverflow.api.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.riverflow.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 接口调用日志
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wf_api_call_log")
public class ApiCallLog extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 接口ID
     */
    private Long apiId;

    /**
     * 接口编码
     */
    private String apiCode;

    /**
     * 日志来源：openapi-开放接口调用，flow-流程API节点调用
     */
    private String source;

    /**
     * 请求URL
     */
    private String requestUrl;

    /**
     * 请求方式
     */
    private String requestMethod;

    /**
     * 请求头JSON
     */
    private String requestHeaders;

    /**
     * 请求体（入参）
     */
    private String requestBody;

    /**
     * 响应体（出参）
     */
    private String responseBody;

    /**
     * HTTP状态码
     */
    private Integer statusCode;

    /**
     * 耗时毫秒
     */
    private Integer costTime;

    /**
     * 调用状态：0-失败 1-成功
     */
    private Integer callStatus;

    /**
     * 错误信息
     */
    private String errorMsg;
}
