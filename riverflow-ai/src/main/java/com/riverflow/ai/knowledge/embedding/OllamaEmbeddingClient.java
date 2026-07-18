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
 * Ollama Embedding 客户端
 *
 * <p>调用 Ollama /api/embed 或 /api/embeddings 接口。Ollama 原生批量能力较弱，
 * 默认逐条调用后合并结果。</p>
 */
@Slf4j
public class OllamaEmbeddingClient implements EmbeddingClient {

    public static final String TYPE = "ollama";
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    private final String baseUrl;
    private final String model;
    private final int dimension;
    private final int timeout;
    private final OkHttpClient httpClient;

    public OllamaEmbeddingClient(String baseUrl, String model, int dimension, int timeout) {
        this.baseUrl = normalizeUrl(baseUrl);
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
        List<float[]> results = new ArrayList<>(texts.size());
        for (String text : texts) {
            if (!StringUtils.hasText(text)) {
                results.add(new float[dimension]);
                continue;
            }
            results.add(embedSingle(text.trim()));
        }
        return results;
    }

    private float[] embedSingle(String text) {
        JSONObject body = new JSONObject();
        body.put("model", model);
        body.put("input", text);

        Request request = new Request.Builder()
                .url(baseUrl + "embed")
                .post(RequestBody.create(JSON.toJSONString(body), JSON_MEDIA_TYPE))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                log.error("Ollama Embedding 调用失败: code={}, body={}", response.code(), responseBody);
                throw new RuntimeException("Ollama Embedding 调用失败: HTTP " + response.code());
            }
            return parseEmbedding(responseBody);
        } catch (IOException e) {
            log.error("Ollama Embedding 调用异常", e);
            throw new RuntimeException("Ollama Embedding 调用异常: " + e.getMessage(), e);
        }
    }

    private float[] parseEmbedding(String responseBody) {
        JSONObject json = JSON.parseObject(responseBody);
        JSONArray embeddings = json.getJSONArray("embeddings");
        if (embeddings == null || embeddings.isEmpty()) {
            // 兼容 /api/embeddings 单条响应格式 { "embedding": [...] }
            JSONArray single = json.getJSONArray("embedding");
            if (single == null) {
                throw new RuntimeException("Ollama Embedding 响应格式异常");
            }
            return toFloatArray(single);
        }
        return toFloatArray(embeddings.getJSONArray(0));
    }

    private float[] toFloatArray(JSONArray array) {
        float[] vector = new float[array.size()];
        for (int i = 0; i < array.size(); i++) {
            vector[i] = array.getFloatValue(i);
        }
        return vector;
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
            throw new IllegalArgumentException("Ollama baseUrl 不能为空");
        }
        String result = url.trim();
        if (!result.endsWith("/")) {
            result = result + "/";
        }
        return result;
    }
}
