package com.riverflow.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.riverflow.api.entity.FlowEdge;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 流程边 Mapper
 */
@Mapper
public interface FlowEdgeMapper extends BaseMapper<FlowEdge> {

    @Select("SELECT * FROM wf_flow_edge WHERE flow_id = #{flowId} AND del_flag = 0 ORDER BY priority")
    List<FlowEdge> selectByFlowId(@Param("flowId") Long flowId);
}
