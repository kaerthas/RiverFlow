package com.riverflow.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.riverflow.api.entity.SysRoleMenu;

import java.util.List;

/**
 * 角色菜单/权限关联 Service
 */
public interface SysRoleMenuService extends IService<SysRoleMenu> {

    /**
     * 根据角色ID查询菜单ID列表
     */
    List<Long> getMenuIdsByRoleId(Long roleId);
}
