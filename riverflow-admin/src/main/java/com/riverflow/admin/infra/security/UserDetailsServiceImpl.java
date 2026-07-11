package com.riverflow.admin.infra.security;

import com.riverflow.admin.service.SysMenuService;
import com.riverflow.admin.service.SysRoleService;
import com.riverflow.admin.service.SysUserService;
import com.riverflow.api.entity.SysRole;
import com.riverflow.api.entity.SysUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户详情服务实现
 * 从数据库加载用户信息、角色、权限
 */
@Slf4j
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    /**
     * 超级管理员角色编码
     */
    public static final String ADMIN_ROLE_CODE = "admin";

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SysRoleService sysRoleService;

    @Autowired
    private SysMenuService sysMenuService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = sysUserService.getByUsername(username);
        if (user == null) {
            log.warn("用户不存在: {}", username);
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        if (user.getStatus() == null || user.getStatus() == 0) {
            log.warn("用户已停用: {}", username);
            throw new UsernameNotFoundException("用户已停用: " + username);
        }

        return buildLoginUser(user);
    }

    /**
     * 根据用户构建 LoginUser
     */
    public LoginUser buildLoginUser(SysUser user) {
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(user.getId());
        loginUser.setUsername(user.getUsername());
        loginUser.setPassword(user.getPassword());
        loginUser.setRealName(user.getRealName());
        loginUser.setAvatar(user.getAvatar());
        loginUser.setDeptId(user.getDeptId());
        loginUser.setDeptName(user.getDeptName());
        loginUser.setStatus(user.getStatus());

        // 查询角色
        List<SysRole> roles = sysRoleService.getRolesByUserId(user.getId());
        Set<String> roleCodes = roles.stream()
                .map(SysRole::getRoleCode)
                .collect(Collectors.toSet());
        loginUser.setRoles(roleCodes);

        // 判断是否为超级管理员
        boolean isAdmin = roleCodes.contains(ADMIN_ROLE_CODE);
        loginUser.setAdmin(isAdmin);

        // 查询权限标识
        List<String> perms = sysMenuService.getPermsByUserId(user.getId());
        loginUser.setPermissions(perms.stream().collect(Collectors.toSet()));

        return loginUser;
    }
}
