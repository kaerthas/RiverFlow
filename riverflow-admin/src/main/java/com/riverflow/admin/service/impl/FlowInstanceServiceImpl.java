package com.riverflow.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.riverflow.admin.mapper.FlowInstanceMapper;
import com.riverflow.admin.service.FlowInstanceService;
import com.riverflow.api.entity.FlowInstance;
import org.springframework.stereotype.Service;

@Service
public class FlowInstanceServiceImpl extends ServiceImpl<FlowInstanceMapper, FlowInstance> implements FlowInstanceService {
}
