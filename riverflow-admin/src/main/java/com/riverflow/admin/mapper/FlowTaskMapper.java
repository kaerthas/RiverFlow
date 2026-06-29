package com.riverflow.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.riverflow.api.entity.FlowTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 流程任务 Mapper
 */
@Mapper
public interface FlowTaskMapper extends BaseMapper<FlowTask> {

    @Select("SELECT * FROM wf_flow_task WHERE instance_id = #{instanceId} AND del_flag = 0 ORDER BY create_time")
    List<FlowTask> selectByInstanceId(@Param("instanceId") Long instanceId);

    @Select("SELECT * FROM wf_flow_task WHERE status IN ('pending', 'waiting') AND (next_execute_time IS NULL OR next_execute_time <= #{now}) " +
            "ORDER BY create_time LIMIT 100")
    List<FlowTask> selectPendingTasks(@Param("now") LocalDateTime now);

    @Select("SELECT * FROM wf_flow_task WHERE instance_id = #{instanceId} AND batch_no = #{batchNo} AND del_flag = 0")
    List<FlowTask> selectByInstanceIdAndBatchNo(@Param("instanceId") Long instanceId, @Param("batchNo") String batchNo);

    @Select("SELECT * FROM wf_flow_task WHERE instance_id = #{instanceId} AND loop_node_id = #{loopNodeId} AND task_type = #{taskType} AND del_flag = 0 ORDER BY iteration_index")
    List<FlowTask> selectByInstanceIdAndLoopNodeIdAndTaskType(@Param("instanceId") Long instanceId,
                                                               @Param("loopNodeId") String loopNodeId,
                                                               @Param("taskType") String taskType);
}
