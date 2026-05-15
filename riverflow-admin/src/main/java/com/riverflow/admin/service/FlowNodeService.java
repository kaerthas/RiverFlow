package com.riverflow.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.riverflow.api.entity.FlowNode;

import java.util.List;

public interface FlowNodeService extends IService<FlowNode> {
    List<FlowNode> getNodesByFlowId(Long flowId);

    int physicalDeleteByFlowId(Long flowId);
}
