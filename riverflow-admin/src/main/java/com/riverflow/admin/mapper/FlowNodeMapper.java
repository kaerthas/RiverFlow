package com.riverflow.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.riverflow.api.entity.FlowNode;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 流程节点 Mapper
 */
@Mapper
public interface FlowNodeMapper extends BaseMapper<FlowNode> {

    @Select("SELECT * FROM wf_flow_node WHERE flow_id = #{flowId} AND del_flag = 0 ORDER BY sort_no")
    List<FlowNode> selectByFlowId(@Param("flowId") Long flowId);

    @Delete("DELETE FROM wf_flow_node WHERE flow_id = #{flowId}")
    int physicalDeleteByFlowId(@Param("flowId") Long flowId);
}
