package com.riverflow.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.riverflow.api.entity.SysUserRole;

import java.util.List;

/**
 * 用户角色关联 Service
 */
public interface SysUserRoleService extends IService<SysUserRole> {

    /**
     * 根据用户ID查询角色ID列表
     */
    List<Long> getRoleIdsByUserId(Long userId);
}
