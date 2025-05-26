package com.inspur.workinfo.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

public class TokenSignUtils {

    private static String signWithSHA256(String signString, String secret) {
        try {
            Mac macSHA256 = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            macSHA256.init(secretKey);
            byte[] hash = macSHA256.doFinal(signString.getBytes());
            return byteToHex(hash);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String byteToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        String temp;
        for (int i = 0; i < bytes.length; i++) {
            temp = Integer.toHexString(bytes[i] & 0xFF);
            if (temp.length() == 1) {
                // 1得到一位的进行补0操作
                sb.append("0");
            }
            sb.append(temp);
        }
        return sb.toString();
    }
}
