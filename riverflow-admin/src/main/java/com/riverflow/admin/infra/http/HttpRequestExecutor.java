package com.riverflow.admin.infra.http;

import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * HTTP 请求执行器抽象基类
 */
@Slf4j
public abstract class HttpRequestExecutor {

    protected final CloseableHttpClient httpClient;

    public HttpRequestExecutor(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * 执行 HTTP 请求
     */
    public JSONObject execute(HttpUriRequest request, int timeout) {
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(timeout)
                .setSocketTimeout(timeout)
                .setConnectionRequestTimeout(timeout)
                .build();
        request.setConfig(config);

        long start = System.currentTimeMillis();
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            int statusCode = response.getStatusLine().getStatusCode();
            String body = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            long cost = System.currentTimeMillis() - start;

            log.debug("HTTP 请求完成: status={}, cost={}ms, url={}", statusCode, cost, request.getURI());

            JSONObject result = new JSONObject();
            result.put("statusCode", statusCode);
            result.put("body", body);
            result.put("cost", cost);
            return result;
        } catch (IOException e) {
            log.error("HTTP 请求失败: url={}", request.getURI(), e);
            throw new RuntimeException("HTTP 请求失败: " + e.getMessage(), e);
        }
    }
}
