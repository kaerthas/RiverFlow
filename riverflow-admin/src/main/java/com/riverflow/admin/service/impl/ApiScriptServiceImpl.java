package com.riverflow.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.riverflow.admin.mapper.ApiScriptMapper;
import com.riverflow.admin.service.ApiScriptService;
import com.riverflow.api.entity.ApiScript;
import org.springframework.stereotype.Service;

@Service
public class ApiScriptServiceImpl extends ServiceImpl<ApiScriptMapper, ApiScript> implements ApiScriptService {
}
