package com.riverflow.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.riverflow.admin.service.SysRoleMenuService;
import com.riverflow.admin.service.SysRoleService;
import com.riverflow.api.entity.SysRole;
import com.riverflow.api.entity.SysRoleMenu;
import com.riverflow.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统角色管理
 */
@Slf4j
@RestController
@RequestMapping("/system/role")
public class SysRoleController {

    @Autowired
    private SysRoleService sysRoleService;

    @Autowired
    private SysRoleMenuService sysRoleMenuService;

    @GetMapping("/list")
    @PreAuthorize("@ss.hasPerm('system:role:list')")
    public R<Page<SysRole>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String roleName,
            @RequestParam(required = false) String roleCode,
            @RequestParam(required = false) Integer status) {
        Page<SysRole> pageParam = new Page<>(page, size);
        QueryWrapper<SysRole> qw = new QueryWrapper<>();
        qw.eq("del_flag", 0);
        if (status != null) qw.eq("status", status);
        if (roleName != null && !roleName.isEmpty()) qw.like("role_name", roleName);
        if (roleCode != null && !roleCode.isEmpty()) qw.like("role_code", roleCode);
        qw.orderByAsc("sort_no");
        return R.ok(sysRoleService.page(pageParam, qw));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@ss.hasPerm('system:role:list')")
    public R<SysRole> getById(@PathVariable Long id) {
        return R.ok(sysRoleService.getById(id));
    }

    @GetMapping("/{id}/menus")
    @PreAuthorize("@ss.hasPerm('system:role:list')")
    public R<List<Long>> getRoleMenus(@PathVariable Long id) {
        return R.ok(sysRoleMenuService.getMenuIdsByRoleId(id));
    }

    @PostMapping
    @PreAuthorize("@ss.hasPerm('system:role:add')")
    public R<Long> save(@RequestBody SysRole role) {
        sysRoleService.save(role);
        return R.ok(role.getId());
    }

    @PutMapping
    @PreAuthorize("@ss.hasPerm('system:role:edit')")
    public R<Long> update(@RequestBody SysRole role) {
        sysRoleService.updateById(role);
        return R.ok(role.getId());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.hasPerm('system:role:delete')")
    public R<Void> delete(@PathVariable Long id) {
        sysRoleService.removeById(id);
        return R.ok();
    }

    @PostMapping("/{id}/menus")
    @PreAuthorize("@ss.hasPerm('system:role:edit')")
    @Transactional(rollbackFor = Exception.class)
    public R<Void> assignMenus(@PathVariable Long id, @RequestBody List<Long> menuIds) {
        // 删除旧关联
        QueryWrapper<SysRoleMenu> qw = new QueryWrapper<>();
        qw.eq("role_id", id);
        sysRoleMenuService.remove(qw);

        // 新增关联
        if (menuIds != null && !menuIds.isEmpty()) {
            List<SysRoleMenu> list = menuIds.stream()
                    .distinct()
                    .map(menuId -> {
                        SysRoleMenu rm = new SysRoleMenu();
                        rm.setRoleId(id);
                        rm.setMenuId(menuId);
                        return rm;
                    })
                    .collect(Collectors.toList());
            sysRoleMenuService.saveBatch(list);
        }
        return R.ok();
    }
}
