package com.riverflow.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.riverflow.api.entity.ApiCallLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 接口调用日志 Mapper
 */
@Mapper
public interface ApiCallLogMapper extends BaseMapper<ApiCallLog> {
}
