package com.riverflow.admin.modules.ai.client;

import com.riverflow.admin.modules.ai.config.AiServiceProperties;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.Enumeration;

/**
 * AI 服务转发客户端
 *
 * <p>将主服务收到的 /ai/** 请求转发给独立的 riverflow-ai 服务。
 * 支持请求头透传（含 JWT）、连接池配置、错误信息提取。
 */
@Slf4j
@Component
public class AiServiceClient {

    private final AiServiceProperties properties;
    private final RestTemplate restTemplate;

    @Autowired
    public AiServiceClient(AiServiceProperties properties) {
        this.properties = properties;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.getTimeout());
        factory.setReadTimeout(properties.getTimeout());
        this.restTemplate = new RestTemplate(factory);
    }

    /**
     * 获取 AI 服务基础地址
     */
    public String getBaseUrl() {
        String baseUrl = properties.getBaseUrl();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl;
    }

    /**
     * 转发请求到 AI 服务
     */
    public String forward(String method, String uri, String body, HttpServletRequest request) {
        String url = getBaseUrl() + uri;

        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            // 跳过 host 和 content-length，避免转发异常
            if ("host".equalsIgnoreCase(name) || "content-length".equalsIgnoreCase(name)) {
                continue;
            }
            headers.set(name, request.getHeader(name));
        }

        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        log.debug("转发 AI 请求: method={}, url={}", method, url);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.valueOf(method),
                    entity,
                    String.class
            );
            return response.getBody();
        } catch (RestClientResponseException e) {
            log.error("AI 服务返回错误: url={}, status={}, body={}", url, e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("AI 服务错误: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("AI 服务调用异常: url={}", url, e);
            throw new RuntimeException("AI 服务调用异常: " + e.getMessage(), e);
        }
    }
}
