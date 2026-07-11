package com.riverflow.admin.infra.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 登录用户信息
 * 封装 Spring Security 所需的 UserDetails 信息
 */
@Data
public class LoginUser implements UserDetails {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 密码
     */
    @JsonIgnore
    private String password;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 状态：0-停用 1-启用
     */
    private Integer status;

    /**
     * 角色编码集合
     */
    private Set<String> roles = new HashSet<>();

    /**
     * 权限标识集合
     */
    private Set<String> permissions = new HashSet<>();

    /**
     * 是否超级管理员
     */
    private boolean admin;

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<String> allAuthorities = new HashSet<>();
        // Spring Security 要求角色以 ROLE_ 开头
        if (roles != null) {
            roles.forEach(role -> {
                if (role != null) {
                    allAuthorities.add(role.startsWith("ROLE_") ? role : "ROLE_" + role);
                }
            });
        }
        // 权限标识也作为 authority 放入，便于 @PreAuthorize("hasAuthority('xxx')") 使用
        if (permissions != null) {
            allAuthorities.addAll(permissions);
        }
        return allAuthorities.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return status != null && status == 1;
    }

    /**
     * 判断是否拥有指定权限
     */
    public boolean hasPermission(String permission) {
        if (admin) {
            return true;
        }
        return permissions != null && permissions.contains(permission);
    }

    /**
     * 判断是否拥有指定角色
     */
    public boolean hasRole(String role) {
        if (admin) {
            return true;
        }
        if (role == null) {
            return false;
        }
        String roleCode = role.startsWith("ROLE_") ? role.substring(5) : role;
        return roles != null && roles.contains(roleCode);
    }
}
