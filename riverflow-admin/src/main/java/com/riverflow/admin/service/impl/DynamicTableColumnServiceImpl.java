package com.riverflow.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.riverflow.admin.mapper.DynamicTableColumnMapper;
import com.riverflow.admin.service.DynamicTableColumnService;
import com.riverflow.api.entity.DynamicTableColumn;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DynamicTableColumnServiceImpl extends ServiceImpl<DynamicTableColumnMapper, DynamicTableColumn> implements DynamicTableColumnService {

    @Override
    public List<DynamicTableColumn> getColumnsByTableId(Long tableId) {
        return baseMapper.selectByTableId(tableId);
    }
}
