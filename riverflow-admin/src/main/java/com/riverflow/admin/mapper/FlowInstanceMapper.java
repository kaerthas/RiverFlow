package com.riverflow.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.riverflow.api.entity.FlowInstance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 流程实例 Mapper
 */
@Mapper
public interface FlowInstanceMapper extends BaseMapper<FlowInstance> {

    @Update("UPDATE wf_flow_instance SET status = #{status}, current_node_id = #{currentNodeId}, " +
            "context_json = #{contextJson}, end_time = #{endTime} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status,
                     @Param("currentNodeId") String currentNodeId,
                     @Param("contextJson") String contextJson,
                     @Param("endTime") java.time.LocalDateTime endTime);
}
