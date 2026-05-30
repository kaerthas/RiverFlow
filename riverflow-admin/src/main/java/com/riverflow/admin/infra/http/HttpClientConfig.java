package com.riverflow.admin.infra.http;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * HTTP 客户端配置
 * 全局连接池 + 超时防护，防止单接口挂掉拖垮整个系统
 */
@Configuration
public class HttpClientConfig {

    /**
     * 连接池最大连接数
     */
    private static final int MAX_TOTAL = 200;

    /**
     * 每个路由（域名+端口）最大连接数
     */
    private static final int MAX_PER_ROUTE = 50;

    /**
     * 建立 TCP 连接超时（毫秒）
     */
    private static final int CONNECT_TIMEOUT_MS = 5_000;

    /**
     * 等待从连接池获取连接的超时（毫秒）
     */
    private static final int CONNECTION_REQUEST_TIMEOUT_MS = 5_000;

    /**
     * Socket 读取超时（毫秒）—— 全局默认值
     * 单接口可在 HttpRequestExecutor 中通过 timeout 参数覆盖
     */
    private static final int SOCKET_TIMEOUT_MS = 30_000;

    /**
     * 空闲连接回收时间（秒）
     */
    private static final int IDLE_EVICT_SECONDS = 30;

    @Bean
    public CloseableHttpClient closeableHttpClient() {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(MAX_TOTAL);
        connectionManager.setDefaultMaxPerRoute(MAX_PER_ROUTE);

        // 全局默认请求配置：防止外部服务无响应时连接/线程被无限占用
        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setConnectTimeout(CONNECT_TIMEOUT_MS)
                .setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT_MS)
                .setSocketTimeout(SOCKET_TIMEOUT_MS)
                .build();

        return HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(defaultRequestConfig)
                .evictIdleConnections(IDLE_EVICT_SECONDS, TimeUnit.SECONDS)
                .build();
    }
}
