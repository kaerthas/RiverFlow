package com.riverflow.admin.infra.http;

import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 纯文本 POST 请求执行器
 * 支持 Content-Type: application/text 或 text/plain
 */
@Slf4j
public class TextPostRequestExecutor extends HttpRequestExecutor {

    public TextPostRequestExecutor(CloseableHttpClient httpClient, RequestExecutionStrategy strategy) {
        super(httpClient, strategy);
    }

    public JSONObject execute(String url, Map<String, String> headers, String textBody, int timeout) throws IOException {
        log.info("Text POST请求: url={}, body={}", url, textBody);
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Content-Type", "application/text;charset=UTF-8");
        if (headers != null) {
            headers.forEach(httpPost::setHeader);
        }
        if (textBody != null) {
            StringEntity entity = new StringEntity(textBody, StandardCharsets.UTF_8);
            entity.setContentType("application/text;charset=UTF-8");
            httpPost.setEntity(entity);
        }
        return execute(httpPost, timeout);
    }
}