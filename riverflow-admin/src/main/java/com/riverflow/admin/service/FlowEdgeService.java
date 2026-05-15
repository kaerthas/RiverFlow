package com.riverflow.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.riverflow.api.entity.FlowEdge;

import java.util.List;

public interface FlowEdgeService extends IService<FlowEdge> {
    List<FlowEdge> getEdgesByFlowId(Long flowId);

    int physicalDeleteByFlowId(Long flowId);
}
