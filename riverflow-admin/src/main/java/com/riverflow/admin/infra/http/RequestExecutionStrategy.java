package com.riverflow.admin.infra.http;

import org.apache.http.client.methods.HttpUriRequest;

/**
 * HTTP 请求执行策略接口
 */
public interface RequestExecutionStrategy {

    /**
     * 执行 HTTP 请求前的处理（如代理设置）
     */
    void prepareRequest(HttpUriRequest request);
}
