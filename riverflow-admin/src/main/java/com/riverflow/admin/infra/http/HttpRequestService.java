package com.riverflow.admin.infra.http;

import com.alibaba.fastjson2.JSONObject;
import com.riverflow.api.entity.ApiCatalog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
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
        String method = apiCatalog.getMethod();
        String url = apiCatalog.getUrl();
        String contentType = apiCatalog.getContentType();
        int timeout = apiCatalog.getTimeout() != null ? apiCatalog.getTimeout() : 30000;

        boolean useProxy = apiCatalog.getProxyEnabled() != null && apiCatalog.getProxyEnabled() == 1;
        String proxyHost = apiCatalog.getProxyHost();
        int proxyPort = apiCatalog.getProxyPort() != null ? apiCatalog.getProxyPort() : 0;

        try {
            JSONObject result;
            switch (method != null ? method.toUpperCase() : "GET") {
                case "GET":
                    result = executorFactory.createGetExecutor(useProxy, proxyHost, proxyPort)
                            .execute(url, headers, (Map<String, String>) body, timeout);
                    break;
                case "POST":
                    if (contentType != null && contentType.contains("xml")) {
                        result = executorFactory.createXmlPostExecutor(useProxy, proxyHost, proxyPort)
                                .execute(url, headers, body != null ? body.toString() : null, timeout);
                    } else if (contentType != null && contentType.contains("form")) {
                        result = executorFactory.createFormPostExecutor(useProxy, proxyHost, proxyPort)
                                .execute(url, headers, (Map<String, String>) body, timeout);
                    } else {
                        result = executorFactory.createJsonPostExecutor(useProxy, proxyHost, proxyPort)
                                .execute(url, headers, body, timeout);
                    }
                    break;
                case "PUT":
                    result = executorFactory.createJsonPostExecutor(useProxy, proxyHost, proxyPort)
                            .execute(url, headers, body, timeout);
                    break;
                case "DELETE":
                    result = executorFactory.createGetExecutor(useProxy, proxyHost, proxyPort)
                            .execute(url, headers, (Map<String, String>) body, timeout);
                    break;
                default:
                    throw new IllegalArgumentException("不支持的请求方式: " + method);
            }
            return result;
        } catch (IOException e) {
            log.error("HTTP 请求执行失败: url={}, method={}", url, method, e);
            JSONObject error = new JSONObject();
            error.put("success", false);
            error.put("error", e.getMessage());
            return error;
        }
    }
}
