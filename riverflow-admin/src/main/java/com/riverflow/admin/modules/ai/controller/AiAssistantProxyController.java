package com.riverflow.admin.modules.ai.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.riverflow.admin.modules.ai.client.AiServiceClient;
import com.riverflow.common.result.R;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

/**
 * AI 助手代理控制器
 *
 * <p>统一暴露 /ai/** 入口，由主服务转发到独立的 riverflow-ai 服务。
 * 这样前端只需访问主服务，权限认证由主服务统一处理。
 * <p>普通接口返回统一包装 R&lt;Object&gt;；SSE 流式接口透传 text/event-stream。
 */
@Slf4j
@RestController
@RequestMapping("/ai/**")
public class AiAssistantProxyController {

    private final AiServiceClient aiServiceClient;
    private final RestTemplate streamRestTemplate;

    @Autowired
    public AiAssistantProxyController(AiServiceClient aiServiceClient) {
        this.aiServiceClient = aiServiceClient;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(600000);
        this.streamRestTemplate = new RestTemplate(factory);
    }

    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public Object proxy(HttpServletRequest request, HttpServletResponse response) {
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        if (StringUtils.hasText(queryString)) {
            uri = uri + "?" + queryString;
        }
        String method = request.getMethod();
        String body = readBodyIfNeeded(request, method);

        if (isSseStream(uri)) {
            proxySse(method, uri, body, request, response);
            return null;
        }
        log.debug("转发 AI 请求: method={}, uri={}", method, uri);
        try {
            String aiResponse = aiServiceClient.forward(method, uri, body, request);
            Object data;
            try {
                JSONObject aiJson = JSON.parseObject(aiResponse);
                // AI 服务本身已经使用 R 统一包装，提取内层 data 避免 R 包 R
                data = aiJson.containsKey("data") ? aiJson.get("data") : aiJson;
            } catch (Exception e) {
                // 非 JSON 响应时直接返回字符串
                data = aiResponse;
            }
            return R.ok(data);
        } catch (Exception e) {
            log.error("AI 服务调用失败: method={}, uri={}", method, uri, e);
            return R.fail("AI 服务调用失败: " + e.getMessage());
        }
    }

    private boolean isSseStream(String uri) {
        return uri != null && (uri.endsWith("/stream") || uri.contains("/stream"));
    }

    private void proxySse(String method, String uri, String body, HttpServletRequest request,
                          HttpServletResponse response) {
        response.setContentType(MediaType.TEXT_EVENT_STREAM_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");

        try (OutputStream out = response.getOutputStream()) {
            String baseUrl = aiServiceClient.getBaseUrl();
            String url = baseUrl + uri;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String name = headerNames.nextElement();
                if ("host".equalsIgnoreCase(name) || "content-length".equalsIgnoreCase(name)
                        || "content-type".equalsIgnoreCase(name)) {
                    continue;
                }
                headers.set(name, request.getHeader(name));
            }

            HttpEntity<String> entity = new HttpEntity<>(body, headers);
            streamRestTemplate.execute(url, HttpMethod.valueOf(method),
                    req -> {
                        req.getHeaders().putAll(entity.getHeaders());
                        if (entity.getBody() != null) {
                            req.getBody().write(entity.getBody().getBytes(StandardCharsets.UTF_8));
                        }
                    },
                    clientHttpResponse -> {
                        try (InputStream in = clientHttpResponse.getBody()) {
                            byte[] buffer = new byte[1024];
                            int n;
                            while ((n = in.read(buffer)) != -1) {
                                out.write(buffer, 0, n);
                                out.flush();
                            }
                        }
                        return null;
                    });
        } catch (IOException e) {
            log.error("SSE 流式转发失败: method={}, uri={}", method, uri, e);
        } catch (Exception e) {
            log.error("SSE 流式转发异常: method={}, uri={}", method, uri, e);
        }
    }

    private String readBodyIfNeeded(HttpServletRequest request, String method) {
        // GET/DELETE 通常没有请求体，直接返回空
        if ("GET".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method)) {
            return null;
        }
        try {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
            return sb.toString();
        } catch (IllegalStateException e) {
            // Spring Filter 已读取过 InputStream，无需重复读取
            log.warn("请求体已被读取过，跳过: method={}, uri={}", method, request.getRequestURI());
            return null;
        } catch (IOException e) {
            log.error("读取 AI 请求体失败", e);
            return null;
        }
    }
}
