package com.riverflow.ai.provider.openai;

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
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * OpenAI 协议兼容的 Provider
 *
 * <p>支持 OpenAI、智谱 GLM、通义千问、Azure OpenAI、Ollama（OpenAI 兼容端点）等
 * 兼容 /v1/chat/completions 的模型服务。
 */
@Slf4j
public class OpenAiProvider implements AiProvider {

    public static final String TYPE = "openai";
    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    private final AiProperties.Provider providerConfig;
    private final OkHttpClient httpClient;

    public OpenAiProvider(AiProperties.Provider providerConfig, int timeout) {
        this.providerConfig = providerConfig;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(timeout, TimeUnit.MILLISECONDS)
                .readTimeout(0, TimeUnit.MILLISECONDS) // 流式输出需要长连接
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
            String responseBody = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                log.error("OpenAI provider 调用失败: provider={}, code={}, body={}",
                        providerConfig.getName(), response.code(), responseBody);
                throw new RuntimeException("OpenAI provider 调用失败: HTTP " + response.code());
            }
            return parseResponse(responseBody, model, providerConfig.getName(), System.currentTimeMillis() - start);
        } catch (Exception e) {
            log.error("OpenAI provider 调用异常: provider={}", providerConfig.getName(), e);
            throw new RuntimeException("OpenAI provider 调用异常: " + e.getMessage(), e);
        }
    }

    @Override
    public void stream(AiChatRequest request, Consumer<String> onData, Consumer<Throwable> onError, Runnable onComplete) {
        Request httpRequest = buildRequest(request, true);

        httpClient.newCall(httpRequest).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                log.error("OpenAI stream 请求失败", e);
                onError.accept(e);
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                try (ResponseBody body = response.body()) {
                    if (!response.isSuccessful() || body == null) {
                        String err = body != null ? body.string() : "HTTP " + response.code();
                        onError.accept(new RuntimeException("OpenAI stream 调用失败: " + err));
                        return;
                    }
                    BufferedReader reader = new BufferedReader(body.charStream());
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (!line.startsWith("data:")) {
                            continue;
                        }
                        String data = line.substring(5).trim();
                        if ("[DONE]".equals(data)) {
                            break;
                        }
                        try {
                            JSONObject json = JSON.parseObject(data);
                            JSONArray choices = json.getJSONArray("choices");
                            if (choices == null || choices.isEmpty()) {
                                continue;
                            }
                            JSONObject delta = choices.getJSONObject(0).getJSONObject("delta");
                            String content = delta != null ? delta.getString("content") : null;
                            if (content != null && !content.isEmpty()) {
                                onData.accept(content);
                            }
                        } catch (Exception e) {
                            log.warn("解析 stream 数据失败: {}", data, e);
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
        body.put("messages", toOpenAiMessages(request.getMessages()));
        if (request.getTemperature() != null) {
            body.put("temperature", request.getTemperature());
        } else if (providerConfig.getTemperature() != null) {
            body.put("temperature", providerConfig.getTemperature());
        }
        if (request.getMaxTokens() != null) {
            body.put("max_tokens", request.getMaxTokens());
        } else if (providerConfig.getMaxTokens() != null) {
            body.put("max_tokens", providerConfig.getMaxTokens());
        }
        if ("json_object".equals(request.getResponseFormat())) {
            body.put("response_format", new JSONObject().fluentPut("type", "json_object"));
        }
        if (stream) {
            body.put("stream", true);
        }

        String url = providerConfig.getBaseUrl() != null ? providerConfig.getBaseUrl().trim() : "";
        if (!url.endsWith("/")) {
            url = url + "/";
        }
        url = url + "chat/completions";

        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .post(RequestBody.create(JSON.toJSONString(body), JSON_MEDIA_TYPE));
        String apiKey = providerConfig.getApiKey() != null ? providerConfig.getApiKey().trim() : null;
        if (apiKey != null && !apiKey.isBlank()) {
            requestBuilder.header("Authorization", "Bearer " + apiKey);
        }
        log.debug("OpenAI request: provider={}, baseUrl={}, model={}, apiKeyPrefix={}",
                providerConfig.getName(), url, model, maskKey(apiKey));
        return requestBuilder.build();
    }

    private String maskKey(String key) {
        if (key == null || key.length() <= 8) {
            return key != null && !key.isBlank() ? "********" : "<empty>";
        }
        return key.substring(0, 4) + "****" + key.substring(key.length() - 4);
    }

    private JSONArray toOpenAiMessages(List<AiMessage> messages) {
        JSONArray array = new JSONArray();
        for (AiMessage msg : messages) {
            array.add(new JSONObject()
                    .fluentPut("role", msg.getRole())
                    .fluentPut("content", msg.getContent()));
        }
        return array;
    }

    private AiChatResponse parseResponse(String responseBody, String model, String providerName, long cost) {
        JSONObject json = JSON.parseObject(responseBody);
        JSONArray choices = json.getJSONArray("choices");
        if (choices == null || choices.isEmpty()) {
            throw new RuntimeException("LLM 响应中 choices 为空");
        }
        JSONObject message = choices.getJSONObject(0).getJSONObject("message");
        String content = message != null ? message.getString("content") : "";

        JSONObject usage = json.getJSONObject("usage");
        Integer promptTokens = usage != null ? usage.getInteger("prompt_tokens") : null;
        Integer completionTokens = usage != null ? usage.getInteger("completion_tokens") : null;
        Integer totalTokens = usage != null ? usage.getInteger("total_tokens") : null;

        return AiChatResponse.builder()
                .content(content)
                .model(json.getString("model") != null ? json.getString("model") : model)
                .promptTokens(promptTokens)
                .completionTokens(completionTokens)
                .totalTokens(totalTokens)
                .responseTimeMs(cost)
                .provider(providerName)
                .build();
    }
}
