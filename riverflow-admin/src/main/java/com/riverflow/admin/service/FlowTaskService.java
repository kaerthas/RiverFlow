package com.riverflow.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.riverflow.api.entity.FlowTask;

import java.time.LocalDateTime;
import java.util.List;

public interface FlowTaskService extends IService<FlowTask> {
    List<FlowTask> getPendingTasks(LocalDateTime now);

    List<FlowTask> listByInstanceIdAndBatchNo(Long instanceId, String batchNo);

    List<FlowTask> listByInstanceIdAndLoopNodeIdAndTaskType(Long instanceId, String loopNodeId, String taskType);
}
