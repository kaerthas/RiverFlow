package com.riverflow.admin.infra.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collection;

/**
 * 权限校验服务
 * 供 Spring Security SpEL 表达式使用，如：@PreAuthorize("@ss.hasPerm('system:user:list')")
 */
@Service("ss")
public class PermissionService {

    /**
     * 判断是否拥有指定权限
     *
     * @param permission 权限标识
     */
    public boolean hasPerm(String permission) {
        if (!StringUtils.hasText(permission)) {
            return false;
        }
        LoginUser loginUser = getLoginUser();
        if (loginUser == null) {
            return false;
        }
        return loginUser.hasPermission(permission);
    }

    /**
     * 判断是否拥有任意一个权限
     *
     * @param permissions 权限标识数组
     */
    public boolean hasAnyPerm(String... permissions) {
        if (permissions == null || permissions.length == 0) {
            return false;
        }
        LoginUser loginUser = getLoginUser();
        if (loginUser == null) {
            return false;
        }
        return Arrays.stream(permissions).anyMatch(loginUser::hasPermission);
    }

    /**
     * 判断是否拥有所有权限
     *
     * @param permissions 权限标识数组
     */
    public boolean hasAllPerm(String... permissions) {
        if (permissions == null || permissions.length == 0) {
            return false;
        }
        LoginUser loginUser = getLoginUser();
        if (loginUser == null) {
            return false;
        }
        return Arrays.stream(permissions).allMatch(loginUser::hasPermission);
    }

    /**
     * 判断是否拥有指定角色
     *
     * @param role 角色编码
     */
    public boolean hasRole(String role) {
        if (!StringUtils.hasText(role)) {
            return false;
        }
        LoginUser loginUser = getLoginUser();
        if (loginUser == null) {
            return false;
        }
        return loginUser.hasRole(role);
    }

    /**
     * 判断是否拥有任意一个角色
     *
     * @param roles 角色编码数组
     */
    public boolean hasAnyRole(String... roles) {
        if (roles == null || roles.length == 0) {
            return false;
        }
        LoginUser loginUser = getLoginUser();
        if (loginUser == null) {
            return false;
        }
        return Arrays.stream(roles).anyMatch(loginUser::hasRole);
    }

    /**
     * 判断当前用户是否为超级管理员
     */
    public boolean isAdmin() {
        LoginUser loginUser = getLoginUser();
        return loginUser != null && loginUser.isAdmin();
    }

    /**
     * 获取当前登录用户
     */
    public LoginUser getLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof LoginUser) {
            return (LoginUser) principal;
        }
        return null;
    }

    /**
     * 获取当前登录用户ID
     */
    public Long getUserId() {
        LoginUser loginUser = getLoginUser();
        return loginUser != null ? loginUser.getUserId() : null;
    }

    /**
     * 获取当前登录用户名
     */
    public String getUsername() {
        LoginUser loginUser = getLoginUser();
        return loginUser != null ? loginUser.getUsername() : null;
    }

    /**
     * 获取当前用户部门ID
     */
    public Long getDeptId() {
        LoginUser loginUser = getLoginUser();
        return loginUser != null ? loginUser.getDeptId() : null;
    }
}
