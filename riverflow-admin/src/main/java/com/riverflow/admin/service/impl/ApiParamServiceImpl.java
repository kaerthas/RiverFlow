package com.riverflow.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.riverflow.admin.mapper.ApiParamMapper;
import com.riverflow.admin.service.ApiParamService;
import com.riverflow.api.entity.ApiParam;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApiParamServiceImpl extends ServiceImpl<ApiParamMapper, ApiParam> implements ApiParamService {

    @Override
    public List<ApiParam> getParamsByApiId(Long apiId) {
        return baseMapper.selectByApiId(apiId);
    }
}
