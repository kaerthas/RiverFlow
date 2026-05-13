package com.riverflow.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.riverflow.admin.mapper.SysOperationLogMapper;
import com.riverflow.admin.service.SysOperationLogService;
import com.riverflow.api.entity.SysOperationLog;
import org.springframework.stereotype.Service;

/**
 * 系统操作日志 Service 实现
 */
@Service
public class SysOperationLogServiceImpl extends ServiceImpl<SysOperationLogMapper, SysOperationLog> implements SysOperationLogService {
}
