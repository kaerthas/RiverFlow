package com.riverflow.ai.knowledge.embedding;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * OpenAI 协议兼容的 Embedding 客户端
 *
 * <p>支持 OpenAI、通义千问、智谱、Azure OpenAI、one-api 等兼容 /v1/embeddings 的服务。</p>
 */
@Slf4j
public class OpenAiEmbeddingClient implements EmbeddingClient {

    public static final String TYPE = "openai";
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    private final String baseUrl;
    private final String apiKey;
    private final String model;
    private final int dimension;
    private final int timeout;
    private final OkHttpClient httpClient;

    public OpenAiEmbeddingClient(String baseUrl, String apiKey, String model, int dimension, int timeout) {
        this.baseUrl = normalizeUrl(baseUrl);
        this.apiKey = apiKey;
        this.model = model;
        this.dimension = dimension;
        this.timeout = timeout;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(timeout, TimeUnit.MILLISECONDS)
                .readTimeout(timeout, TimeUnit.MILLISECONDS)
                .writeTimeout(timeout, TimeUnit.MILLISECONDS)
                .build();
    }

    @Override
    public List<float[]> embed(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> normalized = texts.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .toList();
        if (normalized.isEmpty()) {
            return Collections.emptyList();
        }

        JSONObject body = new JSONObject();
        body.put("model", model);
        body.put("input", normalized);

        Request.Builder builder = new Request.Builder()
                .url(baseUrl + "embeddings")
                .post(RequestBody.create(JSON.toJSONString(body), JSON_MEDIA_TYPE));
        if (StringUtils.hasText(apiKey)) {
            builder.header("Authorization", "Bearer " + apiKey.trim());
        }

        long start = System.currentTimeMillis();
        try (Response response = httpClient.newCall(builder.build()).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                log.error("Embedding 调用失败: code={}, body={}", response.code(), responseBody);
                throw new RuntimeException("Embedding 调用失败: HTTP " + response.code());
            }
            log.debug("Embedding 调用完成: model={}, cost={}ms, texts={}", model, System.currentTimeMillis() - start, normalized.size());
            return parseEmbeddings(responseBody, normalized.size());
        } catch (IOException e) {
            log.error("Embedding 调用异常", e);
            throw new RuntimeException("Embedding 调用异常: " + e.getMessage(), e);
        }
    }

    private List<float[]> parseEmbeddings(String responseBody, int expectSize) {
        JSONObject json = JSON.parseObject(responseBody);
        JSONArray data = json.getJSONArray("data");
        if (data == null || data.isEmpty()) {
            throw new RuntimeException("Embedding 响应中 data 为空");
        }
        List<float[]> result = new ArrayList<>(expectSize);
        for (int i = 0; i < expectSize; i++) {
            result.add(null);
        }
        for (int i = 0; i < data.size(); i++) {
            JSONObject item = data.getJSONObject(i);
            int index = item.getIntValue("index");
            JSONArray embeddingArray = item.getJSONArray("embedding");
            float[] vector = new float[embeddingArray.size()];
            for (int j = 0; j < embeddingArray.size(); j++) {
                vector[j] = embeddingArray.getFloatValue(j);
            }
            result.set(index, vector);
        }
        return result;
    }

    @Override
    public int dimension() {
        return dimension;
    }

    @Override
    public String name() {
        return TYPE;
    }

    private String normalizeUrl(String url) {
        if (!StringUtils.hasText(url)) {
            throw new IllegalArgumentException("Embedding baseUrl 不能为空");
        }
        String result = url.trim();
        if (!result.endsWith("/")) {
            result = result + "/";
        }
        return result;
    }
}
