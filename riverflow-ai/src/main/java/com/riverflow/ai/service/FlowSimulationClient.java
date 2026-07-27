package com.riverflow.ai.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.riverflow.ai.config.AiProperties;
import com.riverflow.api.entity.FlowEdge;
import com.riverflow.api.entity.FlowNode;
import com.riverflow.api.modules.workflow.simulate.FlowSimulationResult;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 流程沙箱模拟执行客户端
 *
 * <p>通过 HTTP 调用 admin 服务的 {@code /workflow/simulate} 接口，对 AI 生成的流程草稿进行沙箱模拟执行。
 */
@Slf4j
@Component
public class FlowSimulationClient {

    private static final MediaType JSON_MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

    private final AiProperties aiProperties;
    private final OkHttpClient httpClient;

    @Autowired
    public FlowSimulationClient(AiProperties aiProperties) {
        this.aiProperties = aiProperties;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(5000, TimeUnit.MILLISECONDS)
                .readTimeout(30000, TimeUnit.MILLISECONDS)
                .writeTimeout(5000, TimeUnit.MILLISECONDS)
                .build();
    }

    /**
     * 调用 admin 沙箱模拟执行接口
     *
     * @param nodes 流程节点
     * @param edges 流程边
     * @param initialContext 初始上下文变量
     * @return 模拟执行结果；调用失败时返回失败的 {@link FlowSimulationResult}
     */
    public FlowSimulationResult simulate(List<FlowNode> nodes, List<FlowEdge> edges,
                                          Map<String, Object> initialContext) {
        String baseUrl = aiProperties.getAdminBaseUrl();
        String url = baseUrl.endsWith("/") ? baseUrl + "workflow/simulate" : baseUrl + "/workflow/simulate";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("nodes", nodes);
        requestBody.put("edges", edges);
        if (initialContext != null && !initialContext.isEmpty()) {
            requestBody.put("initialContext", initialContext);
        }

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(JSON.toJSONString(requestBody), JSON_MEDIA_TYPE))
                .header("Content-Type", "application/json")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            String body = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                log.warn("调用 admin 沙箱模拟执行接口失败: code={}, body={}", response.code(), body);
                return failureResult("调用 admin 沙箱模拟执行接口失败: HTTP " + response.code());
            }
            JSONObject json = JSON.parseObject(body);
            if (json == null) {
                return failureResult("admin 沙箱模拟执行接口返回空响应");
            }
            Integer code = json.getInteger("code");
            if (code != null && code != 200) {
                String msg = json.getString("msg");
                return failureResult("admin 沙箱模拟执行接口返回业务错误: " + (msg != null ? msg : code));
            }
            JSONObject data = json.getJSONObject("data");
            if (data == null) {
                return failureResult("admin 沙箱模拟执行接口未返回 data");
            }
            return data.toJavaObject(FlowSimulationResult.class);
        } catch (Exception e) {
            log.warn("调用 admin 沙箱模拟执行接口异常", e);
            return failureResult("调用 admin 沙箱模拟执行接口异常: " + e.getMessage());
        }
    }

    private FlowSimulationResult failureResult(String errorMsg) {
        FlowSimulationResult result = new FlowSimulationResult();
        result.setSuccess(false);
        result.setErrorMsg(errorMsg);
        return result;
    }
}
