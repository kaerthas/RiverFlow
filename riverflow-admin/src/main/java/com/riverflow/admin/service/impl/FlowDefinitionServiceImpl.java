package com.riverflow.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.riverflow.admin.mapper.FlowDefinitionMapper;
import com.riverflow.admin.service.FlowDefinitionService;
import com.riverflow.api.entity.FlowDefinition;
import org.springframework.stereotype.Service;

@Service
public class FlowDefinitionServiceImpl extends ServiceImpl<FlowDefinitionMapper, FlowDefinition> implements FlowDefinitionService {
}
