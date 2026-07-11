package com.riverflow.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.riverflow.admin.mapper.SysRoleMenuMapper;
import com.riverflow.admin.service.SysRoleMenuService;
import com.riverflow.api.entity.SysRoleMenu;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 角色菜单/权限关联 Service 实现
 */
@Service
public class SysRoleMenuServiceImpl extends ServiceImpl<SysRoleMenuMapper, SysRoleMenu> implements SysRoleMenuService {

    @Override
    public List<Long> getMenuIdsByRoleId(Long roleId) {
        return baseMapper.selectMenuIdsByRoleId(roleId);
    }
}
