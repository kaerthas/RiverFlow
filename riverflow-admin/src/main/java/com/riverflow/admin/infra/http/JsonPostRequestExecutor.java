package com.riverflow.admin.infra.http;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * JSON POST 请求执行器
 */
@Slf4j
public class JsonPostRequestExecutor extends HttpRequestExecutor {

    public JsonPostRequestExecutor(CloseableHttpClient httpClient, RequestExecutionStrategy strategy) {
        super(httpClient, strategy);
    }

    public JSONObject execute(String url, Map<String, String> headers, Object body, int timeout) throws IOException {
        String bodyStr = body != null ? (body instanceof String ? (String) body : JSON.toJSONString(body)) : "null";
        log.info("JSON POST请求: url={}, body={}", url, bodyStr);
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Content-Type", "application/json;charset=UTF-8");
        if (headers != null) {
            headers.forEach(httpPost::setHeader);
        }
        if (body != null) {
            StringEntity entity = new StringEntity(
                    body instanceof String ? (String) body : JSON.toJSONString(body),
                    StandardCharsets.UTF_8);
            entity.setContentType("application/json;charset=UTF-8");
            httpPost.setEntity(entity);
        }
        return execute(httpPost, timeout);
    }
}
