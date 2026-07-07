package com.riverflow.ai.provider.ollama;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.riverflow.ai.config.AiProperties;
import com.riverflow.ai.provider.AiChatRequest;
import com.riverflow.ai.provider.AiChatResponse;
import com.riverflow.ai.provider.AiMessage;
import com.riverflow.ai.provider.AiProvider;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.BufferedReader;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Ollama 原生 API Provider
 *
 * <p>直接调用 Ollama 的 /api/chat 端点，不经过 OpenAI 兼容层。
 */
@Slf4j
public class OllamaNativeProvider implements AiProvider {

    public static final String TYPE = "ollama";
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    private final AiProperties.Provider providerConfig;
    private final OkHttpClient httpClient;

    public OllamaNativeProvider(AiProperties.Provider providerConfig, int timeout) {
        this.providerConfig = providerConfig;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(timeout, TimeUnit.MILLISECONDS)
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .writeTimeout(timeout, TimeUnit.MILLISECONDS)
                .build();
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public boolean supports(String model) {
        return true;
    }

    public String getName() {
        return providerConfig.getName();
    }

    @Override
    public AiChatResponse chat(AiChatRequest request) {
        String model = request.getModel() != null ? request.getModel() : providerConfig.getDefaultModel();
        Request httpRequest = buildRequest(request, false);

        long start = System.currentTimeMillis();
        try (Response response = httpClient.newCall(httpRequest).execute()) {
            String body = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                log.error("Ollama 调用失败: code={}, body={}", response.code(), body);
                throw new RuntimeException("Ollama 调用失败: HTTP " + response.code());
            }
            return parseResponse(body, model, System.currentTimeMillis() - start);
        } catch (Exception e) {
            log.error("Ollama 调用异常", e);
            throw new RuntimeException("Ollama 调用异常: " + e.getMessage(), e);
        }
    }

    @Override
    public void stream(AiChatRequest request, Consumer<String> onData, Consumer<Throwable> onError, Runnable onComplete) {
        Request httpRequest = buildRequest(request, true);
        httpClient.newCall(httpRequest).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, java.io.IOException e) {
                onError.accept(e);
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws java.io.IOException {
                try (ResponseBody body = response.body()) {
                    if (!response.isSuccessful() || body == null) {
                        String err = body != null ? body.string() : "HTTP " + response.code();
                        onError.accept(new RuntimeException("Ollama stream 调用失败: " + err));
                        return;
                    }
                    BufferedReader reader = new BufferedReader(body.charStream());
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.isBlank()) continue;
                        try {
                            JSONObject json = JSON.parseObject(line);
                            JSONObject message = json.getJSONObject("message");
                            if (message == null) continue;
                            String content = message.getString("content");
                            if (content != null && !content.isEmpty()) {
                                onData.accept(content);
                            }
                            if (json.getBooleanValue("done")) {
                                break;
                            }
                        } catch (Exception e) {
                            log.warn("解析 Ollama stream 数据失败: {}", line, e);
                        }
                    }
                    onComplete.run();
                } catch (Exception e) {
                    onError.accept(e);
                }
            }
        });
    }

    private Request buildRequest(AiChatRequest request, boolean stream) {
        String model = request.getModel() != null && !request.getModel().isBlank()
                ? request.getModel() : providerConfig.getDefaultModel();
        JSONObject body = new JSONObject();
        body.put("model", model);
        body.put("messages", toOllamaMessages(request.getMessages()));
        body.put("stream", stream);

        JSONObject options = new JSONObject();
        if (providerConfig.getTemperature() != null) {
            options.put("temperature", providerConfig.getTemperature());
        }
        int contextSize = providerConfig.getContextSize() != null ? providerConfig.getContextSize() : 8192;
        options.put("num_ctx", contextSize);
        body.put("options", options);

        String url = providerConfig.getBaseUrl() != null ? providerConfig.getBaseUrl().trim() : "";
        if (!url.endsWith("/")) {
            url = url + "/";
        }
        url = url + "api/chat";

        return new Request.Builder()
                .url(url)
                .post(RequestBody.create(JSON.toJSONString(body), JSON_MEDIA_TYPE))
                .build();
    }

    private JSONArray toOllamaMessages(List<AiMessage> messages) {
        JSONArray array = new JSONArray();
        for (AiMessage msg : messages) {
            array.add(new JSONObject()
                    .fluentPut("role", msg.getRole())
                    .fluentPut("content", msg.getContent()));
        }
        return array;
    }

    private AiChatResponse parseResponse(String responseBody, String model, long cost) {
        JSONObject json = JSON.parseObject(responseBody);
        JSONObject message = json.getJSONObject("message");
        String content = message != null ? message.getString("content") : "";
        return AiChatResponse.builder()
                .content(content)
                .model(model)
                .responseTimeMs(cost)
                .provider(providerConfig.getName())
                .build();
    }
}
