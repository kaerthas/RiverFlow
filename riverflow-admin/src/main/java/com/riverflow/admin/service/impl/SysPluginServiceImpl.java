package com.riverflow.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.riverflow.admin.mapper.SysPluginMapper;
import com.riverflow.admin.service.SysPluginService;
import com.riverflow.api.entity.SysPlugin;
import org.springframework.stereotype.Service;

@Service
public class SysPluginServiceImpl extends ServiceImpl<SysPluginMapper, SysPlugin> implements SysPluginService {
}
