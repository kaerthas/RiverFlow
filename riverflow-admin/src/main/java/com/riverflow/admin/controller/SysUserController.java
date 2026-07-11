package com.riverflow.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.riverflow.admin.infra.datascope.DataScope;
import com.riverflow.admin.service.SysUserRoleService;
import com.riverflow.admin.service.SysUserService;
import com.riverflow.api.entity.SysUser;
import com.riverflow.api.entity.SysUserRole;
import com.riverflow.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 系统用户管理
 */
@Slf4j
@RestController
@RequestMapping("/system/user")
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SysUserRoleService sysUserRoleService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/list")
    @PreAuthorize("@ss.hasPerm('system:user:list')")
    @DataScope(deptColumn = "dept_id", userColumn = "create_by")
    public R<Page<SysUser>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) Integer status) {
        Page<SysUser> pageParam = new Page<>(page, size);
        QueryWrapper<SysUser> qw = new QueryWrapper<>();
        qw.eq("status", status != null ? status : 1);
        if (username != null && !username.isEmpty()) qw.like("username", username);
        if (realName != null && !realName.isEmpty()) qw.like("real_name", realName);
        qw.orderByDesc("create_time");
        return R.ok(sysUserService.page(pageParam, qw));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@ss.hasPerm('system:user:list')")
    public R<Map<String, Object>> getById(@PathVariable Long id) {
        SysUser user = sysUserService.getById(id);
        if (user == null) {
            return R.fail("用户不存在");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("user", user);
        result.put("roleIds", sysUserRoleService.getRoleIdsByUserId(id));
        return R.ok(result);
    }

    @PostMapping
    @PreAuthorize("@ss.hasPerm('system:user:add')")
    @Transactional(rollbackFor = Exception.class)
    public R<Long> save(@RequestBody Map<String, Object> params) {
        SysUser user = parseUser(params);
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        sysUserService.save(user);

        List<Long> roleIds = parseRoleIds(params);
        saveUserRoles(user.getId(), roleIds);

        return R.ok(user.getId());
    }

    @PutMapping
    @PreAuthorize("@ss.hasPerm('system:user:edit')")
    @Transactional(rollbackFor = Exception.class)
    public R<Long> update(@RequestBody Map<String, Object> params) {
        SysUser user = parseUser(params);
        if (user.getId() == null) {
            return R.fail("用户ID不能为空");
        }
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(null);
        }
        sysUserService.updateById(user);

        List<Long> roleIds = parseRoleIds(params);
        saveUserRoles(user.getId(), roleIds);

        return R.ok(user.getId());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.hasPerm('system:user:delete')")
    @Transactional(rollbackFor = Exception.class)
    public R<Void> delete(@PathVariable Long id) {
        sysUserService.removeById(id);
        QueryWrapper<SysUserRole> qw = new QueryWrapper<>();
        qw.eq("user_id", id);
        sysUserRoleService.remove(qw);
        return R.ok();
    }

    @GetMapping("/all")
    @PreAuthorize("@ss.hasPerm('system:user:list')")
    public R<List<SysUser>> all() {
        QueryWrapper<SysUser> qw = new QueryWrapper<>();
        qw.eq("status", 1);
        qw.orderByAsc("username");
        return R.ok(sysUserService.list(qw));
    }

    /**
     * 保存用户角色关联
     */
    private void saveUserRoles(Long userId, List<Long> roleIds) {
        QueryWrapper<SysUserRole> qw = new QueryWrapper<>();
        qw.eq("user_id", userId);
        sysUserRoleService.remove(qw);

        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        List<SysUserRole> list = roleIds.stream()
                .distinct()
                .map(roleId -> {
                    SysUserRole ur = new SysUserRole();
                    ur.setUserId(userId);
                    ur.setRoleId(roleId);
                    return ur;
                })
                .collect(Collectors.toList());
        sysUserRoleService.saveBatch(list);
    }

    private SysUser parseUser(Map<String, Object> params) {
        SysUser user = new SysUser();
        if (params.get("id") != null) {
            user.setId(Long.valueOf(params.get("id").toString()));
        }
        user.setUsername((String) params.get("username"));
        user.setPassword((String) params.get("password"));
        user.setRealName((String) params.get("realName"));
        user.setPhone((String) params.get("phone"));
        user.setEmail((String) params.get("email"));
        user.setAvatar((String) params.get("avatar"));
        user.setDeptId(params.get("deptId") != null ? Long.valueOf(params.get("deptId").toString()) : null);
        user.setDeptName((String) params.get("deptName"));
        user.setStatus(params.get("status") != null ? Integer.valueOf(params.get("status").toString()) : 1);
        return user;
    }

    @SuppressWarnings("unchecked")
    private List<Long> parseRoleIds(Map<String, Object> params) {
        Object roleIdsObj = params.get("roleIds");
        if (roleIdsObj == null) {
            return new ArrayList<>();
        }
        if (roleIdsObj instanceof List) {
            return ((List<Object>) roleIdsObj).stream()
                    .map(o -> Long.valueOf(o.toString()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
