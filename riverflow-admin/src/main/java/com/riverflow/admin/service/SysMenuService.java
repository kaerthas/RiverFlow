package com.riverflow.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.riverflow.api.entity.SysMenu;

import java.util.List;

/**
 * 系统菜单/权限 Service
 */
public interface SysMenuService extends IService<SysMenu> {

    /**
     * 根据用户ID查询权限标识列表
     */
    List<String> getPermsByUserId(Long userId);

    /**
     * 根据用户ID查询菜单列表
     */
    List<SysMenu> getMenusByUserId(Long userId);

    /**
     * 根据角色ID查询权限标识列表
     */
    List<String> getPermsByRoleId(Long roleId);
}
