package com.riverflow.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.riverflow.api.entity.FlowLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 流程日志 Mapper
 */
@Mapper
public interface FlowLogMapper extends BaseMapper<FlowLog> {
}
