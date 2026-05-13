package com.riverflow.admin.infra.http;

import org.apache.http.client.methods.HttpUriRequest;

/**
 * 直连执行策略
 */
public class DirectExecutionStrategy implements RequestExecutionStrategy {

    @Override
    public void prepareRequest(HttpUriRequest request) {
        // 直连模式无需额外处理
    }
}
