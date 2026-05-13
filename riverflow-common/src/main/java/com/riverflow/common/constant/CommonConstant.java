package com.riverflow.common.constant;

/**
 * 全局常量
 */
public final class CommonConstant {

    private CommonConstant() {
    }

    /**
     * 正常状态
     */
    public static final Integer STATUS_NORMAL = 0;

    /**
     * 停用状态
     */
    public static final Integer STATUS_DISABLE = 1;

    /**
     * 是
     */
    public static final Integer YES = 1;

    /**
     * 否
     */
    public static final Integer NO = 0;

    /**
     * JWT Token 请求头
     */
    public static final String TOKEN_HEADER = "Authorization";

    /**
     * Token 前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * UTF-8 编码
     */
    public static final String UTF8 = "UTF-8";

    /**
     * 流程上下文变量前缀
     */
    public static final String CONTEXT_PREFIX = "context.";

    /**
     * 流程分布式锁前缀
     */
    public static final String FLOW_LOCK_PREFIX = "riverflow:lock:flow:";

    /**
     * 流程实例上下文缓存前缀
     */
    public static final String FLOW_CONTEXT_PREFIX = "riverflow:context:";
}
