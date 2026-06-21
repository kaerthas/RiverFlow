package com.riverflow.admin.infra.openapi;

import lombok.extern.slf4j.Slf4j;

import jakarta.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * 从请求中提取客户端真实 IP
 * <p>
 * 优先读取反向代理常见的 Header，最后回退到 remoteAddr。
 * 注意：生产环境应在网关/Nginx 层确保这些 Header 可信，防止客户端伪造。
 * 本类通过 {@link OpenApiAuthProperties#getTrustedProxies()} 限制只有来自可信代理的请求才读取代理 Header。
 */
@Slf4j
public class RealIpExtractor {

    private static final List<String> PROXY_HEADERS = Arrays.asList(
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED"
    );

    private static final String UNKNOWN = "unknown";

    /**
     * 获取真实 IP
     *
     * @param request        请求
     * @param trustedProxies 可信代理 IP/CIDR 集合；为空表示不信任任何代理 Header
     */
    public static String extract(HttpServletRequest request, Set<String> trustedProxies) {
        String remoteAddr = request.getRemoteAddr();

        // 将 IPv6 回环统一为 127.0.0.1，便于白名单匹配
        if ("0:0:0:0:0:0:0:1".equals(remoteAddr)) {
            remoteAddr = "127.0.0.1";
        }

        // 只有来源是可信代理时才读取代理 Header
        if (trustedProxies != null && !trustedProxies.isEmpty()
                && isTrustedProxy(remoteAddr, trustedProxies)) {
            for (String header : PROXY_HEADERS) {
                String value = request.getHeader(header);
                if (value != null && !value.isEmpty() && !UNKNOWN.equalsIgnoreCase(value)) {
                    // X-Forwarded-For 可能包含多个 IP，取第一个（最靠近客户端）
                    int commaIndex = value.indexOf(',');
                    String ip = commaIndex > 0 ? value.substring(0, commaIndex).trim() : value.trim();
                    if (isValidIp(ip)) {
                        return ip;
                    }
                }
            }
        }

        if (isValidIp(remoteAddr)) {
            return remoteAddr;
        }

        return request.getRemoteAddr();
    }

    /**
     * 判断 remoteAddr 是否来自可信代理
     */
    private static boolean isTrustedProxy(String remoteAddr, Set<String> trustedProxies) {
        for (String rule : trustedProxies) {
            if (rule == null || rule.trim().isEmpty()) {
                continue;
            }
            try {
                if (IpWhitelistChecker.check(rule, remoteAddr)) {
                    return true;
                }
            } catch (Exception e) {
                log.warn("可信代理规则解析失败: rule={}", rule, e);
            }
        }
        return false;
    }

    /**
     * 简单校验 IP 格式
     */
    private static boolean isValidIp(String ip) {
        if (ip == null || ip.isEmpty() || UNKNOWN.equalsIgnoreCase(ip)) {
            return false;
        }
        try {
            InetAddress.getByName(ip);
            return true;
        } catch (UnknownHostException e) {
            log.warn("无法解析的 IP: {}", ip);
            return false;
        }
    }
}
