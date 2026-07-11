package com.riverflow.admin.infra.datascope;

import com.riverflow.admin.infra.security.PermissionService;
import com.riverflow.admin.service.SysDeptService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 数据权限 AOP 切面
 * 在标注了 @DataScope 的方法执行前后设置/清理数据权限上下文
 */
@Aspect
@Component
@Order(-1)
@Slf4j
public class DataScopeAspect {

    private final SysDeptService sysDeptService;
    private final PermissionService permissionService;

    public DataScopeAspect(SysDeptService sysDeptService, PermissionService permissionService) {
        this.sysDeptService = sysDeptService;
        this.permissionService = permissionService;
    }

    @Around("@annotation(com.riverflow.admin.infra.datascope.DataScope) || @within(com.riverflow.admin.infra.datascope.DataScope)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        DataScope dataScope = method.getAnnotation(DataScope.class);
        if (dataScope == null) {
            dataScope = method.getDeclaringClass().getAnnotation(DataScope.class);
        }
        if (dataScope == null) {
            return point.proceed();
        }

        int scope = dataScope.scope();
        Set<Long> deptIds = Collections.emptySet();
        Long currentDeptId = permissionService.getDeptId();

        switch (scope) {
            case DataScopeScope.ALL:
                break;
            case DataScopeScope.DEPT_ONLY:
                if (currentDeptId != null) {
                    deptIds = Collections.singleton(currentDeptId);
                }
                break;
            case DataScopeScope.DEPT_AND_CHILD:
                if (currentDeptId != null) {
                    deptIds = sysDeptService.getChildDeptIds(currentDeptId);
                }
                break;
            case DataScopeScope.SELF_ONLY:
                break;
            case DataScopeScope.CUSTOM:
                deptIds = parseCustomDeptIds(dataScope.customDeptIds());
                break;
            default:
                break;
        }

        DataScopeContextHolder.DataScopeConfig config = new DataScopeContextHolder.DataScopeConfig(
                scope, dataScope.deptColumn(), dataScope.userColumn(), deptIds);
        DataScopeContextHolder.set(config);
        log.debug("开启数据权限: method={}, scope={}, deptColumn={}, userColumn={}, deptIds={}",
                method.getName(), scope, config.getDeptColumn(), config.getUserColumn(), deptIds);

        try {
            return point.proceed();
        } finally {
            DataScopeContextHolder.clear();
            log.debug("清理数据权限上下文");
        }
    }

    private Set<Long> parseCustomDeptIds(String[] deptIds) {
        if (deptIds == null || deptIds.length == 0) {
            return Collections.emptySet();
        }
        return Arrays.stream(deptIds)
                .filter(StringUtils::hasText)
                .map(Long::valueOf)
                .collect(Collectors.toSet());
    }
}
