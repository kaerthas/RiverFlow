package com.riverflow.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.riverflow.admin.mapper.SysMenuMapper;
import com.riverflow.admin.service.SysMenuService;
import com.riverflow.api.entity.SysMenu;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 系统菜单/权限 Service 实现
 */
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    @Override
    public List<String> getPermsByUserId(Long userId) {
        return baseMapper.selectPermsByUserId(userId);
    }

    @Override
    public List<SysMenu> getMenusByUserId(Long userId) {
        return baseMapper.selectMenusByUserId(userId);
    }

    @Override
    public List<String> getPermsByRoleId(Long roleId) {
        return baseMapper.selectPermsByRoleId(roleId);
    }
}
