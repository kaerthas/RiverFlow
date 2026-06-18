package com.riverflow.admin.infra.openapi;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

/**
 * 开放接口签名工具
 * <p>
 * 签名规则（AppKey + AppSecret + HmacSHA256）：
 * 1. 调用方在 Header 中携带：
 *    X-AppKey: 应用标识
 *    X-Timestamp: 当前时间戳（秒）
 *    X-Nonce: 随机字符串（建议 UUID，长度 8-32）
 *    X-Signature: 签名结果
 * 2. 待签名字符串：
 *    appKey={appKey}&nonce={nonce}&timestamp={timestamp}&body={bodyString}
 *    其中 bodyString 为请求体原始字符串；GET/DELETE 无请求体时为空字符串。
 * 3. 签名算法：HmacSHA256(appSecret, stringToSign)，结果转小写十六进制。
 */
@Slf4j
public class OpenApiSignatureUtil {

    private static final String HMAC_SHA256 = "HmacSHA256";

    /**
     * 生成签名字符串（不计算 HMAC，仅拼接待签名字符串）
     */
    public static String buildStringToSign(String appKey, String timestamp, String nonce, String bodyString) {
        String body = bodyString == null ? "" : bodyString;
        return "appKey=" + appKey
                + "&nonce=" + (nonce == null ? "" : nonce)
                + "&timestamp=" + (timestamp == null ? "" : timestamp)
                + "&body=" + body;
    }

    /**
     * 生成 HmacSHA256 签名
     */
    public static String sign(String appSecret, String stringToSign) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec keySpec = new SecretKeySpec(appSecret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
            mac.init(keySpec);
            byte[] bytes = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(bytes);
        } catch (Exception e) {
            log.error("HmacSHA256 签名失败", e);
            throw new RuntimeException("签名失败", e);
        }
    }

    /**
     * 验证签名
     */
    public static boolean verify(String appKey, String appSecret, String timestamp, String nonce,
                                 String bodyString, String signature) {
        if (appKey == null || appSecret == null || signature == null) {
            return false;
        }
        String stringToSign = buildStringToSign(appKey, timestamp, nonce, bodyString);
        String expected = sign(appSecret, stringToSign);
        return constantTimeEquals(expected, signature);
    }

    /**
     * 将 Map 按 key 字典序拼接为 query string（用于 form-urlencoded 场景）
     */
    public static String canonicalQueryString(Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return "";
        }
        TreeMap<String, Object> sorted = new TreeMap<>(params);
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : sorted.entrySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(entry.getKey()).append("=");
            if (entry.getValue() != null) {
                sb.append(entry.getValue());
            }
        }
        return sb.toString();
    }

    /**
     * 字节数组转小写十六进制
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * 常量时间比较，防止时序攻击
     */
    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        byte[] aBytes = a.getBytes(StandardCharsets.UTF_8);
        byte[] bBytes = b.getBytes(StandardCharsets.UTF_8);
        if (aBytes.length != bBytes.length) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < aBytes.length; i++) {
            result |= aBytes[i] ^ bBytes[i];
        }
        return result == 0;
    }
}
