package com.riverflow.common.result;

import lombok.Getter;

/**
 * 响应状态码枚举
 */
@Getter
public enum ResultCode {

    /**
     * 成功
     */
    SUCCESS(200, "操作成功"),

    /**
     * 参数错误
     */
    PARAM_ERROR(400, "参数错误"),

    /**
     * 未授权
     */
    UNAUTHORIZED(401, "未授权，请先登录"),

    /**
     * 禁止访问
     */
    FORBIDDEN(403, "拒绝访问"),

    /**
     * 资源不存在
     */
    NOT_FOUND(404, "资源不存在"),

    /**
     * 业务异常
     */
    BUSINESS_ERROR(500, "业务处理失败"),

    /**
     * 系统异常
     */
    SYSTEM_ERROR(503, "系统繁忙，请稍后重试"),

    /**
     * 流程引擎异常
     */
    FLOW_ENGINE_ERROR(1001, "流程引擎执行异常"),

    /**
     * 节点执行失败
     */
    NODE_EXECUTE_ERROR(1002, "节点执行失败"),

    /**
     * 条件表达式错误
     */
    CONDITION_ERROR(1003, "条件表达式解析错误"),

    /**
     * 数据源连接失败
     */
    DATASOURCE_ERROR(2001, "数据源连接失败"),

    /**
     * 接口调用失败
     */
    API_CALL_ERROR(3001, "接口调用失败");

    private final int code;
    private final String msg;

    ResultCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
