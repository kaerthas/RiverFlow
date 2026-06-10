package com.riverflow.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.riverflow.admin.mapper.ApiAppMapper;
import com.riverflow.admin.service.ApiAppService;
import com.riverflow.api.entity.ApiApp;
import org.springframework.stereotype.Service;

@Service
public class ApiAppServiceImpl extends ServiceImpl<ApiAppMapper, ApiApp> implements ApiAppService {
}
