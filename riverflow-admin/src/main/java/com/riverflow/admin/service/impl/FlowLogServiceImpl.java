package com.riverflow.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.riverflow.admin.mapper.FlowLogMapper;
import com.riverflow.admin.service.FlowLogService;
import com.riverflow.api.entity.FlowLog;
import org.springframework.stereotype.Service;

@Service
public class FlowLogServiceImpl extends ServiceImpl<FlowLogMapper, FlowLog> implements FlowLogService {
}
