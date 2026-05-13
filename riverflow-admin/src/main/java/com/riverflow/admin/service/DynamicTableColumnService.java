package com.riverflow.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.riverflow.api.entity.DynamicTableColumn;

import java.util.List;

public interface DynamicTableColumnService extends IService<DynamicTableColumn> {
    List<DynamicTableColumn> getColumnsByTableId(Long tableId);
}
