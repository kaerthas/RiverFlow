package com.riverflow.gateway.filter;

import com.alibaba.fastjson2.JSON;
import com.riverflow.common.result.R;
import com.riverflow.gateway.security.JwtTokenUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * JWT 初筛过滤器（方案 4.2 "双保险" 第一阶段）
 *
 * <p>实现 WebFilter 而非 GlobalFilter：WebFilter 在 DispatcherHandler 层面对所有请求生效，
 * 既覆盖被路由转发的请求，也覆盖网关本地端点（/gateway/route/** 路由管理接口），
 * 避免管理端点绕过认证。</p>
 *
 * <p>校验通过后，将用户上下文以 X-User-Id / X-User-Name 请求头透传给下游服务。
 * riverflow-admin 侧 Spring Security 保持原样，继续自行解析 Authorization 头，
 * 形成双保险；待运行稳定后再将 admin 内 JWT 解析逻辑移除。</p>
 *
 * <p>免认证白名单与 admin 侧 SecurityConfig 的放行路径对齐，但匹配的是
 * 剥离前缀之前的原始路径（如 /api/login、/admin/login）。</p>
 */
@Slf4j
@Component
public class JwtAuthWebFilter implements WebFilter, Ordered {

    /**
     * 免认证路径（匹配未剥前缀的原始路径）
     */
    private static final List<String> WHITE_LIST = Arrays.asList(
            // 登录、刷新 Token、验证码（/api 为兼容前缀，/admin 为规范前缀）
            "/api/login", "/api/refresh", "/api/captcha/**",
            "/admin/login", "/admin/refresh", "/admin/captcha/**",
            // 对外开放接口：由 admin 侧 OpenApiAuthFilter 做 AppKey/签名认证
            "/open/**",
            // Knife4j 文档资源
            "/doc.html", "/webjars/**", "/swagger-resources/**", "/v3/api-docs/**", "/favicon.ico",
            // 健康检查（供 Nginx / 监控探活）
            "/actuator/health", "/actuator/health/**"
    );

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Resource
    private JwtTokenUtil jwtTokenUtil;

    @Value("${jwt.header:Authorization}")
    private String header;

    @Value("${jwt.token-prefix:Bearer }")
    private String tokenPrefix;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        // CORS 预检与白名单直接放行
        if (HttpMethod.OPTIONS == request.getMethod() || isWhite(path)) {
            return chain.filter(exchange);
        }

        String token = resolveToken(request);
        Claims claims = token == null ? null : jwtTokenUtil.parse(token);
        // Refresh Token 不允许访问业务接口
        if (claims == null || "refresh".equals(claims.get("tokenType"))) {
            log.debug("[Gateway] 未通过 JWT 初筛: {} {}", request.getMethodValue(), path);
            return unauthorized(exchange);
        }

        // 透传用户上下文 Header（下游服务可选消费）
        ServerHttpRequest.Builder builder = request.mutate()
                .header("X-User-Name", String.valueOf(claims.getSubject()));
        Object userId = claims.get("userId");
        if (userId != null) {
            builder.header("X-User-Id", String.valueOf(userId));
        }
        return chain.filter(exchange.mutate().request(builder.build()).build());
    }

    private boolean isWhite(String path) {
        for (String pattern : WHITE_LIST) {
            if (PATH_MATCHER.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    private String resolveToken(ServerHttpRequest request) {
        String value = request.getHeaders().getFirst(header);
        if (!StringUtils.hasText(value)) {
            return null;
        }
        if (StringUtils.hasText(tokenPrefix) && value.startsWith(tokenPrefix)) {
            return value.substring(tokenPrefix.length());
        }
        return value;
    }

    /**
     * 与 riverflow-admin 认证入口保持一致的响应：HTTP 401 + R 格式 JSON
     */
    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
        byte[] bytes = JSON.toJSONString(R.fail(401, "未登录或Token已过期")).getBytes(StandardCharsets.UTF_8);
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
