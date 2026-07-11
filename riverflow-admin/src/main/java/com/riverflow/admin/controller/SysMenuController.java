package com.riverflow.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.riverflow.admin.mapper.SysMenuMapper;
import com.riverflow.admin.service.SysMenuService;
import com.riverflow.api.entity.SysMenu;
import com.riverflow.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统菜单/权限管理
 */
@Slf4j
@RestController
@RequestMapping("/system/menu")
public class SysMenuController {

    @Autowired
    private SysMenuService sysMenuService;

    @Autowired
    private SysMenuMapper sysMenuMapper;

    @GetMapping("/list")
    @PreAuthorize("@ss.hasPerm('system:menu:list')")
    public R<List<SysMenu>> list(@RequestParam(required = false) String menuName,
                                 @RequestParam(required = false) Integer menuType,
                                 @RequestParam(required = false) Integer status) {
        QueryWrapper<SysMenu> qw = new QueryWrapper<>();
        qw.eq("del_flag", 0);
        if (status != null) qw.eq("status", status);
        if (menuType != null) qw.eq("menu_type", menuType);
        if (menuName != null && !menuName.isEmpty()) qw.like("menu_name", menuName);
        qw.orderByAsc("sort_no");
        // 返回扁平列表，由前端统一构建树形结构（便于上级菜单选择器也使用同一份数据）
        List<SysMenu> menus = sysMenuService.list(qw);
        return R.ok(menus);
    }

    @GetMapping("/all")
    @PreAuthorize("@ss.hasPerm('system:menu:list')")
    public R<List<SysMenu>> all() {
        QueryWrapper<SysMenu> qw = new QueryWrapper<>();
        qw.eq("del_flag", 0);
        qw.eq("status", 1);
        qw.orderByAsc("sort_no");
        return R.ok(sysMenuService.list(qw));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@ss.hasPerm('system:menu:list')")
    public R<SysMenu> getById(@PathVariable Long id) {
        return R.ok(sysMenuService.getById(id));
    }

    @PostMapping
    @PreAuthorize("@ss.hasPerm('system:menu:add')")
    public R<Long> save(@RequestBody SysMenu menu) {
        // 清理已逻辑删除的同名权限，避免 uk_perms 唯一索引冲突
        clearDeletedPerms(menu.getPerms());
        if (isPermsDuplicate(null, menu.getPerms())) {
            return R.fail("权限标识已存在：" + menu.getPerms());
        }
        sysMenuService.save(menu);
        return R.ok(menu.getId());
    }

    @PutMapping
    @PreAuthorize("@ss.hasPerm('system:menu:edit')")
    public R<Long> update(@RequestBody SysMenu menu) {
        // 清理已逻辑删除的同名权限，避免 uk_perms 唯一索引冲突
        clearDeletedPerms(menu.getPerms());
        if (isPermsDuplicate(menu.getId(), menu.getPerms())) {
            return R.fail("权限标识已存在：" + menu.getPerms());
        }
        sysMenuService.updateById(menu);
        return R.ok(menu.getId());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.hasPerm('system:menu:delete')")
    public R<Void> delete(@PathVariable Long id) {
        // 检查是否存在子菜单
        QueryWrapper<SysMenu> qw = new QueryWrapper<>();
        qw.eq("parent_id", id);
        qw.eq("del_flag", 0);
        long count = sysMenuService.count(qw);
        if (count > 0) {
            return R.fail("存在子菜单，无法删除");
        }
        sysMenuService.removeById(id);
        return R.ok();
    }

    /**
     * 清理已逻辑删除的同名权限记录，避免再次添加时触发 uk_perms 唯一索引
     */
    private void clearDeletedPerms(String perms) {
        if (!StringUtils.hasText(perms)) {
            return;
        }
        sysMenuMapper.physicalDeleteByPerms(perms);
    }

    /**
     * 校验权限标识是否重复
     */
    private boolean isPermsDuplicate(Long id, String perms) {
        if (!StringUtils.hasText(perms)) {
            return false;
        }
        QueryWrapper<SysMenu> qw = new QueryWrapper<>();
        qw.eq("perms", perms);
        qw.eq("del_flag", 0);
        if (id != null) {
            qw.ne("id", id);
        }
        return sysMenuService.count(qw) > 0;
    }

}
