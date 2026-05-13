package com.riverflow.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.riverflow.api.entity.ApiParam;

import java.util.List;

public interface ApiParamService extends IService<ApiParam> {
    List<ApiParam> getParamsByApiId(Long apiId);
}
