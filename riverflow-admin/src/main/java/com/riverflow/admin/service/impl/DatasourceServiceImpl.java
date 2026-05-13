package com.riverflow.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.riverflow.admin.mapper.DatasourceMapper;
import com.riverflow.admin.service.DatasourceService;
import com.riverflow.api.entity.Datasource;
import org.springframework.stereotype.Service;

@Service
public class DatasourceServiceImpl extends ServiceImpl<DatasourceMapper, Datasource> implements DatasourceService {
}
