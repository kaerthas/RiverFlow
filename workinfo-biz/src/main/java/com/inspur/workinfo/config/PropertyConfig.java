package com.inspur.workinfo.config;


import lombok.AllArgsConstructor;
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

    private String NODE_ENV = "dev";

    private String disabilityAllowanceAppId;
    private String disabilityAllowanceAppKey;
    private String disabilityAllowanceUrl;

    /**
     * 协同调度相关配置
     */
    private String dispatchAppId;
    private String dispatchTenancyId;
    private String dispatchUrl;


    /**
     * 网盘相关配置
     */
    private String downloadUrl;
    private String webDiskAppCode;
    private String webDiskDecryptKey;


    /****
     * 互联网区代理模式
     * *****/
    private  String httpProxyIP;
    private  String httpPort;

    public PropertyConfig() {

    }
}
