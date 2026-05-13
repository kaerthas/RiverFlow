package com.riverflow.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.riverflow.admin.mapper.ApiCatalogMapper;
import com.riverflow.admin.service.ApiCatalogService;
import com.riverflow.api.entity.ApiCatalog;
import org.springframework.stereotype.Service;

@Service
public class ApiCatalogServiceImpl extends ServiceImpl<ApiCatalogMapper, ApiCatalog> implements ApiCatalogService {
}
