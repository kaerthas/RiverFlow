package com.riverflow.admin.infra.http;

import com.alibaba.fastjson2.JSONObject;
import com.riverflow.api.entity.ApiCatalog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP 请求统一服务
 * 根据接口配置自动选择执行器和策略
 */
@Slf4j
@Service
public class HttpRequestService {

    @Autowired
    private HttpRequestExecutorFactory executorFactory;

    /**
     * 执行接口调用
     *
     * @param apiCatalog 接口配置
     * @param headers    请求头
     * @param body       请求体
     * @return 响应结果
     */
    public JSONObject execute(ApiCatalog apiCatalog, Map<String, String> headers, Object body) {
        return execute(apiCatalog, headers, body, null);
    }

    public JSONObject execute(ApiCatalog apiCatalog, Map<String, String> headers, Object body, Map<String, String> queryParams) {
        String method = apiCatalog.getMethod();
        String url = apiCatalog.getUrl();
        String contentType = apiCatalog.getContentType();
        int timeout = apiCatalog.getTimeout() != null ? apiCatalog.getTimeout() : 30000;

        boolean useProxy = apiCatalog.getProxyEnabled() != null && apiCatalog.getProxyEnabled() == 1;
        String proxyHost = apiCatalog.getProxyHost();
        int proxyPort = apiCatalog.getProxyPort() != null ? apiCatalog.getProxyPort() : 0;

        // 将 query 参数拼接到 URL
        String finalUrl = buildUrlWithQueryParams(url, queryParams);
        log.info("HTTP请求准备: url={}, method={}, contentType={}, body={}", finalUrl, method, contentType, body);

        try {
            JSONObject result;
            switch (method != null ? method.toUpperCase() : "GET") {
                case "GET":
                    result = executorFactory.createGetExecutor(useProxy, proxyHost, proxyPort)
                            .execute(finalUrl, headers, convertToStringMap(body), timeout);
                    break;
                case "POST":
                    if (contentType != null && contentType.contains("xml")) {
                        result = executorFactory.createXmlPostExecutor(useProxy, proxyHost, proxyPort)
                                .execute(finalUrl, headers, body != null ? body.toString() : null, timeout);
                    } else if (contentType != null && contentType.contains("form")) {
                        result = executorFactory.createFormPostExecutor(useProxy, proxyHost, proxyPort)
                                .execute(finalUrl, headers, convertToStringMap(body), timeout);
                    } else {
                        result = executorFactory.createJsonPostExecutor(useProxy, proxyHost, proxyPort)
                                .execute(finalUrl, headers, body, timeout);
                    }
                    break;
                case "PUT":
                    result = executorFactory.createJsonPostExecutor(useProxy, proxyHost, proxyPort)
                            .execute(finalUrl, headers, body, timeout);
                    break;
                case "DELETE":
                    result = executorFactory.createGetExecutor(useProxy, proxyHost, proxyPort)
                            .execute(finalUrl, headers, convertToStringMap(body), timeout);
                    break;
                default:
                    throw new IllegalArgumentException("不支持的请求方式: " + method);
            }
            return result;
        } catch (IOException e) {
            log.error("HTTP 请求执行失败: url={}, method={}", url, method, e);
            throw new RuntimeException("HTTP 请求执行失败: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> convertToStringMap(Object body) {
        if (body == null) {
            return new HashMap<>();
        }
        if (body instanceof Map) {
            Map<String, String> result = new HashMap<>();
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) body).entrySet()) {
                result.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
            }
            return result;
        }
        if (body instanceof JSONObject) {
            Map<String, String> result = new HashMap<>();
            for (Map.Entry<String, Object> entry : ((JSONObject) body).entrySet()) {
                result.put(entry.getKey(), String.valueOf(entry.getValue()));
            }
            return result;
        }
        return new HashMap<>();
    }

    private static String buildUrlWithQueryParams(String url, Map<String, String> queryParams) {
        if (queryParams == null || queryParams.isEmpty()) {
            return url;
        }
        StringBuilder sb = new StringBuilder(url);
        sb.append(url.contains("?") ? "&" : "?");
        for (Map.Entry<String, String> entry : queryParams.entrySet()) {
            sb.append(urlEncode(entry.getKey()))
                    .append("=")
                    .append(urlEncode(entry.getValue()))
                    .append("&");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    private static String urlEncode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 encoding not supported", e);
        }
    }
}
