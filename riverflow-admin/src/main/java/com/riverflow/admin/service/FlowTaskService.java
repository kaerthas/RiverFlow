package com.riverflow.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.riverflow.api.entity.FlowTask;

import java.time.LocalDateTime;
import java.util.List;

public interface FlowTaskService extends IService<FlowTask> {
    List<FlowTask> getPendingTasks(LocalDateTime now);

    List<FlowTask> getPendingTasks(LocalDateTime now, int limit);

    /**
     * 乐观锁认领任务
     *
     * @return true-认领成功，false-已被认领
     */
    boolean claimTask(Long id, Integer version, String executeNode);

    List<FlowTask> listByInstanceIdAndBatchNo(Long instanceId, String batchNo);

    List<FlowTask> listByInstanceIdAndLoopNodeIdAndTaskType(Long instanceId, String loopNodeId, String taskType);
}
