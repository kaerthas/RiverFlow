package com.riverflow.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.riverflow.admin.mapper.FlowEdgeMapper;
import com.riverflow.admin.service.FlowEdgeService;
import com.riverflow.api.entity.FlowEdge;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FlowEdgeServiceImpl extends ServiceImpl<FlowEdgeMapper, FlowEdge> implements FlowEdgeService {

    @Override
    public List<FlowEdge> getEdgesByFlowId(Long flowId) {
        return baseMapper.selectByFlowId(flowId);
    }
}
