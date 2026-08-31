package com.riverflow.admin.infra.http;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
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

    /**
     * 是否信任所有 HTTPS 证书（跳过证书链与主机名校验）。
     * 平台常需对接内网/自签名证书系统，默认开启；仅对接公网可信证书时可关闭。
     */
    @Value("${riverflow.http.trust-all-ssl:true}")
    private boolean trustAllSsl;

    @Bean
    public CloseableHttpClient closeableHttpClient() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        PoolingHttpClientConnectionManager connectionManager;
        if (trustAllSsl) {
            SSLContext sslContext = SSLContexts.custom()
                    .loadTrustMaterial(null, TrustAllStrategy.INSTANCE)
                    .build();
            SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
                    sslContext, NoopHostnameVerifier.INSTANCE);
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.getSocketFactory())
                    .register("https", sslSocketFactory)
                    .build();
            connectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        } else {
            connectionManager = new PoolingHttpClientConnectionManager();
        }
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
