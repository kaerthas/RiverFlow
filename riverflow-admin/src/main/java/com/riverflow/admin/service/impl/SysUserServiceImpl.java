package com.riverflow.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.riverflow.admin.mapper.SysUserMapper;
import com.riverflow.admin.service.SysUserService;
import com.riverflow.api.entity.SysUser;
import org.springframework.stereotype.Service;

/**
 * 系统用户 Service 实现
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Override
    public SysUser getByUsername(String username) {
        return baseMapper.selectByUsername(username);
    }
}
