package com.riverflow.admin.infra.http;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * HTTP 客户端配置
 */
@Configuration
public class HttpClientConfig {

    @Bean
    public CloseableHttpClient closeableHttpClient() {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(200);
        connectionManager.setDefaultMaxPerRoute(50);

        return HttpClients.custom()
                .setConnectionManager(connectionManager)
                .evictIdleConnections(30, java.util.concurrent.TimeUnit.SECONDS)
                .build();
    }
}
