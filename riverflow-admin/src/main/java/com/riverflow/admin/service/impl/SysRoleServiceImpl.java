package com.riverflow.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.riverflow.admin.mapper.SysRoleMapper;
import com.riverflow.admin.service.SysRoleService;
import com.riverflow.api.entity.SysRole;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 系统角色 Service 实现
 */
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    @Override
    public List<SysRole> getRolesByUserId(Long userId) {
        return baseMapper.selectRolesByUserId(userId);
    }

    @Override
    public SysRole getByRoleCode(String roleCode) {
        return baseMapper.selectByRoleCode(roleCode);
    }
}
