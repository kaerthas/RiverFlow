package com.riverflow.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.riverflow.admin.mapper.SysUserRoleMapper;
import com.riverflow.admin.service.SysUserRoleService;
import com.riverflow.api.entity.SysUserRole;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户角色关联 Service 实现
 */
@Service
public class SysUserRoleServiceImpl extends ServiceImpl<SysUserRoleMapper, SysUserRole> implements SysUserRoleService {

    @Override
    public List<Long> getRoleIdsByUserId(Long userId) {
        return baseMapper.selectRoleIdsByUserId(userId);
    }
}
