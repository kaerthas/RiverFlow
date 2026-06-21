package com.riverflow.admin.config;

import com.riverflow.admin.infra.security.JwtAuthenticationFilter;
import com.riverflow.common.result.R;
import com.riverflow.common.result.ResultCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 配置
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private com.riverflow.admin.infra.openapi.OpenApiAuthFilter openApiAuthFilter;

    /**
     * 安全过滤链
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 启用 CORS，禁用 CSRF（前后端分离，使用 JWT）
            .cors().and()
            .csrf().disable()
            // 禁用 Session，使用无状态 JWT
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            // 禁用默认 logout（项目使用自定义 /logout Controller）
            .logout().disable()
            // 异常处理
            .exceptionHandling()
            .authenticationEntryPoint(authenticationEntryPoint())
            .accessDeniedHandler(accessDeniedHandler())
            .and()
            // 请求授权
            .authorizeHttpRequests(auth -> auth
            // 允许匿名访问的路径
            .requestMatchers("/login").permitAll()
            .requestMatchers("/refresh").permitAll()
            .requestMatchers("/captcha/**").permitAll()
            .requestMatchers("/doc.html").permitAll()
            .requestMatchers("/webjars/**").permitAll()
            .requestMatchers("/swagger-resources/**").permitAll()
            .requestMatchers("/v3/api-docs/**").permitAll()
            .requestMatchers("/open/**").permitAll()  // 由 OpenApiAuthFilter 做应用级认证
            .requestMatchers("/example/**").denyAll()  // 示例/调试接口禁止外部访问
            .requestMatchers("/plugin/**").authenticated()  // 插件管理需要登录
            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
            // 其他请求需要认证
            .anyRequest().authenticated()
            );

        // 添加 JWT 过滤器
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // 添加开放接口认证过滤器，放在 JWT 之前
        http.addFilterBefore(openApiAuthFilter, JwtAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 认证管理器
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * 未认证处理：前后端分离项目，始终返回 JSON 401，由前端处理跳转
     */
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(401);
            response.getWriter().write(com.alibaba.fastjson2.JSON.toJSONString(
                R.fail(ResultCode.UNAUTHORIZED.getCode(), "未登录或Token已过期")));
        };
    }

    /**
     * 无权限处理
     */
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(403);
            response.getWriter().write(com.alibaba.fastjson2.JSON.toJSONString(
                R.fail(ResultCode.FORBIDDEN.getCode(), "无权访问")));
        };
    }
}
