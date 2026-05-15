package com.riverflow.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.riverflow.admin.mapper.FlowNodeMapper;
import com.riverflow.admin.service.FlowNodeService;
import com.riverflow.api.entity.FlowNode;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FlowNodeServiceImpl extends ServiceImpl<FlowNodeMapper, FlowNode> implements FlowNodeService {

    @Override
    public List<FlowNode> getNodesByFlowId(Long flowId) {
        return baseMapper.selectByFlowId(flowId);
    }

    @Override
    public int physicalDeleteByFlowId(Long flowId) {
        return baseMapper.physicalDeleteByFlowId(flowId);
    }
}
