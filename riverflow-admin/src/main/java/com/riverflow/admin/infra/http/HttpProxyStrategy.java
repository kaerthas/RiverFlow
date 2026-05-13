package com.riverflow.admin.infra.http;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.config.RequestConfig;

/**
 * HTTP 代理执行策略
 */
@Slf4j
public class HttpProxyStrategy implements RequestExecutionStrategy {

    private final String proxyHost;
    private final int proxyPort;

    public HttpProxyStrategy(String proxyHost, int proxyPort) {
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
    }

    @Override
    public void prepareRequest(HttpUriRequest request) {
        HttpHost proxy = new HttpHost(proxyHost, proxyPort);
        RequestConfig config = RequestConfig.custom()
                .setProxy(proxy)
                .build();
        request.setConfig(config);
        log.debug("使用代理: {}:{}", proxyHost, proxyPort);
    }
}
