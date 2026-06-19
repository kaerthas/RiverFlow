package com.riverflow.admin.infra.openapi;

import com.alibaba.fastjson2.JSON;
import com.riverflow.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 开放接口认证过滤器
 * <p>
 * 拦截 /open/** 请求，根据 wf_api_catalog 配置的 auth_type 与 allowed_ips
 * 执行应用级认证与 IP 白名单校验。
 */
@Slf4j
@Component
public class OpenApiAuthFilter extends OncePerRequestFilter {

    @Autowired
    private OpenApiAuthService openApiAuthService;

    @Autowired
    private OpenApiAuthProperties authProperties;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();

        // 只处理 /open 路径
        String openPrefix = contextPath + "/open";
        if (!uri.startsWith(openPrefix)) {
            filterChain.doFilter(request, response);
            return;
        }

        // multipart/form-data 不参与签名认证（body 含 boundary，难以规范化）
        String contentType = request.getContentType();
        if (contentType != null && contentType.toLowerCase().startsWith("multipart/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 包装请求，使 body 可重复读取
        RepeatableReadRequestWrapper wrappedRequest = new RepeatableReadRequestWrapper(request);

        String method = wrappedRequest.getMethod();
        String openPath = uri.substring(openPrefix.length());

        OpenApiAuthResult result;
        if ("/flow/start".equals(openPath) || "/flow/executeSync".equals(openPath)) {
            // 固定流程接口
            result = openApiAuthService.authenticateFlow(wrappedRequest);
        } else {
            // 动态开放接口
            result = openApiAuthService.authenticateDynamic(openPath, method, wrappedRequest);
        }

        if (!result.isPassed()) {
            writeAuthError(response, result);
            return;
        }

        filterChain.doFilter(wrappedRequest, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        if (!authProperties.isEnabled()) {
            return true;
        }
        return super.shouldNotFilter(request);
    }

    private void writeAuthError(HttpServletResponse response, OpenApiAuthResult result) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(result.getCode());
        R<Object> r = R.fail(result.getCode(), result.getMessage());
        response.getWriter().write(JSON.toJSONString(r));
    }
}
