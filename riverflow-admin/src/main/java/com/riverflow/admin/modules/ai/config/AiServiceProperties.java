package com.riverflow.admin.modules.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI 服务调用配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "riverflow.ai-service")
public class AiServiceProperties {

    /**
     * AI 服务基础地址
     */
    private String baseUrl = "http://localhost:8081";

    /**
     * 调用超时（毫秒）
     */
    private int timeout = 60000;
}
