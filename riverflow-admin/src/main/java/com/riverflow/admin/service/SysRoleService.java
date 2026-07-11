package com.riverflow.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.riverflow.api.entity.SysRole;

import java.util.List;

/**
 * 系统角色 Service
 */
public interface SysRoleService extends IService<SysRole> {

    /**
     * 根据用户ID查询角色列表
     */
    List<SysRole> getRolesByUserId(Long userId);

    /**
     * 根据角色编码查询角色
     */
    SysRole getByRoleCode(String roleCode);
}
