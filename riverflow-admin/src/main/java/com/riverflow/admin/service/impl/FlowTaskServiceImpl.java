package com.riverflow.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.riverflow.admin.mapper.FlowTaskMapper;
import com.riverflow.admin.service.FlowTaskService;
import com.riverflow.api.entity.FlowTask;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FlowTaskServiceImpl extends ServiceImpl<FlowTaskMapper, FlowTask> implements FlowTaskService {

    @Override
    public List<FlowTask> getPendingTasks(LocalDateTime now) {
        return baseMapper.selectPendingTasks(now);
    }

    @Override
    public List<FlowTask> listByInstanceIdAndBatchNo(Long instanceId, String batchNo) {
        return baseMapper.selectByInstanceIdAndBatchNo(instanceId, batchNo);
    }

    @Override
    public List<FlowTask> listByInstanceIdAndLoopNodeIdAndTaskType(Long instanceId, String loopNodeId, String taskType) {
        return baseMapper.selectByInstanceIdAndLoopNodeIdAndTaskType(instanceId, loopNodeId, taskType);
    }
}
