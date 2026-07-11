package com.riverflow.admin.infra.security;

import com.riverflow.common.constant.CommonConstant;
import com.riverflow.common.result.R;
import com.riverflow.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT 认证过滤器
 * 从请求头中提取 Token 并设置 SecurityContext
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);

        if (StringUtils.hasText(token)) {
            if (jwtUtil.validateToken(token)) {
                String username = jwtUtil.getUsernameFromToken(token);
                Long userId = jwtUtil.getUserIdFromToken(token);

                if (StringUtils.hasText(username) && SecurityContextHolder.getContext().getAuthentication() == null) {
                    try {
                        LoginUser loginUser = (LoginUser) userDetailsService.loadUserByUsername(username);

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(loginUser, userId, loginUser.getAuthorities());
                        authentication.setDetails(userId);
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        log.debug("JWT 认证成功: user={}, uri={}", username, request.getRequestURI());
                    } catch (Exception e) {
                        log.warn("JWT 用户加载失败: user={}, error={}", username, e.getMessage());
                        response.setContentType("application/json;charset=UTF-8");
                        response.setStatus(401);
                        response.getWriter().write(com.alibaba.fastjson2.JSON.toJSONString(
                                R.fail(ResultCode.UNAUTHORIZED.getCode(), "用户认证信息无效")));
                        return;
                    }
                }
            } else {
                // Token 存在但无效（过期或格式错误），直接返回 401，不要继续走认证链触发 302 重定向
                log.warn("JWT 验证失败: uri={}, token 过期或无效", request.getRequestURI());
                response.setContentType("application/json;charset=UTF-8");
                response.setStatus(401);
                response.getWriter().write(com.alibaba.fastjson2.JSON.toJSONString(
                        R.fail(ResultCode.UNAUTHORIZED.getCode(), "未登录或Token已过期")));
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头中提取 Token
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(CommonConstant.TOKEN_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(CommonConstant.TOKEN_PREFIX)) {
            return bearerToken.substring(CommonConstant.TOKEN_PREFIX.length());
        }
        return null;
    }
}
