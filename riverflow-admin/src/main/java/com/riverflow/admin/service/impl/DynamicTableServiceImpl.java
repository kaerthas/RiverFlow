package com.riverflow.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.riverflow.admin.mapper.DynamicTableMapper;
import com.riverflow.admin.service.DynamicTableService;
import com.riverflow.api.entity.DynamicTable;
import org.springframework.stereotype.Service;

@Service
public class DynamicTableServiceImpl extends ServiceImpl<DynamicTableMapper, DynamicTable> implements DynamicTableService {
}
