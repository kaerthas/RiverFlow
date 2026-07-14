package com.riverflow.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.riverflow.api.entity.FlowTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

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

    @Select("SELECT * FROM wf_flow_task WHERE status IN ('pending', 'waiting') AND (next_execute_time IS NULL OR next_execute_time <= #{now}) " +
            "ORDER BY create_time LIMIT #{limit}")
    List<FlowTask> selectPendingTasksWithLimit(@Param("now") LocalDateTime now, @Param("limit") int limit);

    /**
     * 乐观锁认领任务：把 pending 任务更新为 running，并记录执行节点与执行时间。
     *
     * @return 影响行数，1 表示认领成功，0 表示已被其他线程认领
     */
    @Update("UPDATE wf_flow_task " +
            "SET status = 'running', version = version + 1, execute_node = #{executeNode}, execute_time = NOW(), update_time = NOW() " +
            "WHERE id = #{id} AND status = 'pending' AND version = #{version}")
    int claimTask(@Param("id") Long id, @Param("version") Integer version, @Param("executeNode") String executeNode);

    @Select("SELECT * FROM wf_flow_task WHERE instance_id = #{instanceId} AND batch_no = #{batchNo} AND del_flag = 0")
    List<FlowTask> selectByInstanceIdAndBatchNo(@Param("instanceId") Long instanceId, @Param("batchNo") String batchNo);

    @Select("SELECT * FROM wf_flow_task WHERE instance_id = #{instanceId} AND loop_node_id = #{loopNodeId} AND task_type = #{taskType} AND del_flag = 0 ORDER BY iteration_index")
    List<FlowTask> selectByInstanceIdAndLoopNodeIdAndTaskType(@Param("instanceId") Long instanceId,
                                                               @Param("loopNodeId") String loopNodeId,
                                                               @Param("taskType") String taskType);
}
