package com.riverflow.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * 网关侧 JWT 解析工具
 *
 * <p>仅做 Token 有效性初筛，不签发 Token；密钥必须与 riverflow-admin 保持一致，
 * 否则初筛会全部失败。</p>
 */
@Slf4j
@Component
public class JwtTokenUtil {

    @Value("${jwt.secret:riverflow-jwt-secret-key-2024-spring-boot}")
    private String secret;

    private SecretKey key;

    @PostConstruct
    public void init() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        // JJWT 0.11.x 要求 HMAC-SHA 密钥至少 256 bits (32 bytes)
        if (keyBytes.length < 32) {
            log.warn("[Gateway] JWT secret 长度不足 32 字节，已生成随机密钥（与 admin 不一致会导致校验失败，请检查配置）");
            this.key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        } else {
            this.key = Keys.hmacShaKeyFor(keyBytes);
        }
    }

    /**
     * 解析 Token，无效或过期返回 null
     */
    public Claims parse(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.debug("[Gateway] JWT 解析失败: {}", e.getMessage());
            return null;
        }
    }
}
