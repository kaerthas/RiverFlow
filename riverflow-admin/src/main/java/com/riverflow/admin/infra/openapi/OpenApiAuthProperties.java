package com.riverflow.admin.infra.openapi;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * 开放接口认证配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "riverflow.open")
public class OpenApiAuthProperties {

    /**
     * 是否启用开放接口认证过滤器
     */
    private boolean enabled = true;

    /**
     * 时间戳有效期（秒），默认 5 分钟
     */
    private long timestampTolerance = 300;

    /**
     * nonce 在 Redis 中的过期时间（秒），应大于 timestampTolerance
     */
    private long nonceExpire = 600;

    /**
     * 固定流程接口 /open/flow/** 是否强制要求应用级签名认证
     */
    private boolean flowAuthEnabled = true;

    /**
     * 当接口 auth_type 为空时，默认采用的认证策略：none 表示放行，sign 表示必须签名
     */
    private String defaultAuthType = "none";

    /**
     * 可信代理 IP / CIDR 列表。
     * 只有请求来源 remoteAddr 在这些网段内时，才会读取 X-Forwarded-For 等代理 Header。
     * 默认包含本地回环和常见内网段。
     */
    private Set<String> trustedProxies = new HashSet<String>() {{
        add("127.0.0.1");
        add("0:0:0:0:0:0:0:1");
        add("10.0.0.0/8");
        add("172.16.0.0/12");
        add("192.168.0.0/16");
    }};
}
