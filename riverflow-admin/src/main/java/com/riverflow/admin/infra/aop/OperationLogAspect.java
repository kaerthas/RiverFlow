package com.riverflow.admin.infra.aop;

import com.alibaba.fastjson2.JSON;
import com.riverflow.admin.infra.security.JwtUtil;
import com.riverflow.admin.service.SysOperationLogService;
import com.riverflow.api.entity.SysOperationLog;
import com.riverflow.common.annotation.OperationLog;
import com.riverflow.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 操作日志 AOP 切面
 */
@Slf4j
@Aspect
@Component
public class OperationLogAspect {

    @Autowired
    private SysOperationLogService operationLogService;

    @Autowired
    private JwtUtil jwtUtil;

    @Pointcut("@annotation(com.riverflow.common.annotation.OperationLog)")
    public void logPointCut() {
    }

    @Around("logPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long startTime = System.currentTimeMillis();

        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        OperationLog operationLog = method.getAnnotation(OperationLog.class);

        HttpServletRequest request = getRequest();
        String requestParams = getRequestParams(point);

        SysOperationLog logEntity = new SysOperationLog();
        logEntity.setModule(operationLog.module());
        logEntity.setOperation(operationLog.operation());
        logEntity.setMethod(point.getTarget().getClass().getName() + "." + method.getName());
        logEntity.setRequestMethod(request != null ? request.getMethod() : null);
        logEntity.setRequestUrl(request != null ? request.getRequestURI() : null);
        logEntity.setRequestParams(operationLog.logParams() ? requestParams : null);
        logEntity.setIp(getClientIp(request));
        logEntity.setUsername(getCurrentUsername(request));
        logEntity.setStatus(1);
        logEntity.setCreateTime(LocalDateTime.now());

        Object result = null;
        try {
            result = point.proceed();
            long executeTime = System.currentTimeMillis() - startTime;
            logEntity.setExecuteTime(executeTime);

            if (operationLog.logResponse() && result instanceof R) {
                R<?> r = (R<?>) result;
                logEntity.setResponseCode(r.getCode());
                logEntity.setResponseMsg(r.getMsg());
                if (r.getCode() != 200) {
                    logEntity.setStatus(0);
                }
            }
        } catch (Throwable e) {
            long executeTime = System.currentTimeMillis() - startTime;
            logEntity.setExecuteTime(executeTime);
            logEntity.setStatus(0);
            logEntity.setErrorMsg(e.getMessage());
            throw e;
        } finally {
            try {
                operationLogService.save(logEntity);
            } catch (Exception ex) {
                log.error("保存操作日志失败: {}", ex.getMessage());
            }
        }

        return result;
    }

    private HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    private String getRequestParams(ProceedingJoinPoint point) {
        Object[] args = point.getArgs();
        try {
            return JSON.toJSONString(args);
        } catch (Exception e) {
            return "[参数序列化失败]";
        }
    }

    private String getClientIp(HttpServletRequest request) {
        if (request == null) return null;
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    private String getCurrentUsername(HttpServletRequest request) {
        if (request == null) return null;
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.validateToken(token)) {
                return jwtUtil.getUsernameFromToken(token);
            }
        }
        return null;
    }
}
