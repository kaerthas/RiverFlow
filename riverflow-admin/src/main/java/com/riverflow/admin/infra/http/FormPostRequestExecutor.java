package com.riverflow.admin.infra.http;

import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Form POST 请求执行器
 */
@Slf4j
public class FormPostRequestExecutor extends HttpRequestExecutor {

    public FormPostRequestExecutor(CloseableHttpClient httpClient, RequestExecutionStrategy strategy) {
        super(httpClient, strategy);
    }

    public JSONObject execute(String url, Map<String, String> headers, Map<String, String> params, int timeout) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
        if (headers != null) {
            headers.forEach(httpPost::setHeader);
        }
        if (params != null && !params.isEmpty()) {
            List<BasicNameValuePair> pairs = new ArrayList<>();
            params.forEach((k, v) -> pairs.add(new BasicNameValuePair(k, v)));
            httpPost.setEntity(new UrlEncodedFormEntity(pairs, StandardCharsets.UTF_8));
        }
        return execute(httpPost, timeout);
    }
}
