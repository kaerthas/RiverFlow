package com.riverflow.admin.infra.openapi;

import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * IP 白名单校验器
 * <p>
 * 支持单个 IP 与 CIDR 网段，多个规则用逗号分隔。
 * 例如：10.0.0.0/24,192.168.1.10,127.0.0.1
 */
@Slf4j
public class IpWhitelistChecker {

    /**
     * 校验 IP 是否在白名单内
     *
     * @param allowedIps 白名单规则，多个用逗号分隔；null 或空字符串表示不限制
     * @param clientIp   客户端真实 IP
     * @return true-通过；false-拒绝
     */
    public static boolean check(String allowedIps, String clientIp) {
        if (allowedIps == null || allowedIps.trim().isEmpty()) {
            return true; // 未配置表示不限制
        }
        if (clientIp == null || clientIp.trim().isEmpty()) {
            return false;
        }

        String[] rules = allowedIps.split(",");
        for (String rule : rules) {
            String r = rule.trim();
            if (r.isEmpty()) {
                continue;
            }
            try {
                if (matchRule(r, clientIp)) {
                    return true;
                }
            } catch (Exception e) {
                log.warn("IP 白名单规则解析失败: rule={}, clientIp={}, error={}", r, clientIp, e.getMessage());
            }
        }
        return false;
    }

    private static boolean matchRule(String rule, String clientIp) throws UnknownHostException {
        if (rule.contains("/")) {
            return matchCidr(rule, clientIp);
        }
        return rule.equals(clientIp);
    }

    private static boolean matchCidr(String cidr, String clientIp) throws UnknownHostException {
        String[] parts = cidr.split("/");
        if (parts.length != 2) {
            return false;
        }
        String networkIp = parts[0].trim();
        int prefixLength;
        try {
            prefixLength = Integer.parseInt(parts[1].trim());
        } catch (NumberFormatException e) {
            return false;
        }

        InetAddress network = InetAddress.getByName(networkIp);
        InetAddress client = InetAddress.getByName(clientIp);

        // 只支持 IPv4
        byte[] networkBytes = network.getAddress();
        byte[] clientBytes = client.getAddress();
        if (networkBytes.length != clientBytes.length) {
            return false;
        }

        int fullBytes = prefixLength / 8;
        int remainingBits = prefixLength % 8;

        for (int i = 0; i < fullBytes; i++) {
            if (networkBytes[i] != clientBytes[i]) {
                return false;
            }
        }

        if (remainingBits > 0) {
            int mask = 0xFF << (8 - remainingBits);
            return (networkBytes[fullBytes] & mask) == (clientBytes[fullBytes] & mask);
        }

        return true;
    }

    /**
     * 将 IP 字节数组转为整数（辅助方法，备用）
     */
    private static int ipToInt(byte[] bytes) {
        if (bytes == null || bytes.length != 4) {
            return 0;
        }
        ByteBuffer buffer = ByteBuffer.wrap(Arrays.copyOf(bytes, 4));
        return buffer.getInt();
    }
}
