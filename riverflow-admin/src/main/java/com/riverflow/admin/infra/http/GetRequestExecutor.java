package com.riverflow.admin.infra.http;

import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * GET 请求执行器
 */
@Slf4j
public class GetRequestExecutor extends HttpRequestExecutor {

    public GetRequestExecutor(CloseableHttpClient httpClient, RequestExecutionStrategy strategy) {
        super(httpClient, strategy);
    }

    public JSONObject execute(String url, Map<String, String> headers, Map<String, String> params, int timeout) throws IOException {
        StringBuilder fullUrl = new StringBuilder(url);
        if (params != null && !params.isEmpty()) {
            fullUrl.append("?");
            for (Map.Entry<String, String> entry : params.entrySet()) {
                fullUrl.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.name()))
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.name()))
                        .append("&");
            }
            fullUrl.deleteCharAt(fullUrl.length() - 1);
        }

        HttpGet httpGet = new HttpGet(fullUrl.toString());
        if (headers != null) {
            headers.forEach(httpGet::setHeader);
        }
        return execute(httpGet, timeout);
    }
}
