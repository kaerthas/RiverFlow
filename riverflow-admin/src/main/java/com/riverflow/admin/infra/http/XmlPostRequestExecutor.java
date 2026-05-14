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
 * XML POST 请求执行器
 */
@Slf4j
public class XmlPostRequestExecutor extends HttpRequestExecutor {

    public XmlPostRequestExecutor(CloseableHttpClient httpClient) {
        super(httpClient);
    }

    public XmlPostRequestExecutor(CloseableHttpClient httpClient, RequestExecutionStrategy strategy) {
        super(httpClient, strategy);
    }

    public JSONObject execute(String url, Map<String, String> headers, String xmlBody, int timeout) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader("Content-Type", "application/xml;charset=UTF-8");
        if (headers != null) {
            headers.forEach(httpPost::setHeader);
        }
        if (xmlBody != null) {
            StringEntity entity = new StringEntity(xmlBody, StandardCharsets.UTF_8);
            entity.setContentType("application/xml;charset=UTF-8");
            httpPost.setEntity(entity);
        }
        return execute(httpPost, timeout);
    }
}
