package com.inspur.workinfo.config;


import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
**一件事相关配置类
 **/
@Slf4j
@Configuration
@Data
@PropertySource("classpath:config/constant.properties")
@ConfigurationProperties(prefix = "onething")
public class PropertyConfig {

    /**
     * 两残相关配置
     */
    private String disabilityAllowanceAppId;
    private String disabilityAllowanceAppKey;
    private String disabilityAllowanceUrl;

    /**
     * 协同调度相关配置
     */
    private String dispatchAppId;
    private String dispatchTenancyId;
    private String dispatchUrl;

}
