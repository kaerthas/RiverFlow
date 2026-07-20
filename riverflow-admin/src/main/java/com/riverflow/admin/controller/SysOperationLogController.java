package com.riverflow.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.riverflow.admin.service.SysOperationLogService;
import com.riverflow.api.entity.SysOperationLog;
import com.riverflow.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统操作日志管理
 */
@Slf4j
@RestController
@RequestMapping("/operation-log")
public class SysOperationLogController {

    @Autowired
    private SysOperationLogService sysOperationLogService;

    @GetMapping("/list")
    @PreAuthorize("@ss.hasPerm('system:log:list')")
    public R<Page<SysOperationLog>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String operation,
            @RequestParam(required = false) String username) {
        Page<SysOperationLog> pageParam = new Page<>(page, size);
        QueryWrapper<SysOperationLog> qw = new QueryWrapper<>();
        if (module != null && !module.isEmpty()) qw.like("module", module);
        if (operation != null && !operation.isEmpty()) qw.like("operation", operation);
        if (username != null && !username.isEmpty()) qw.like("username", username);
        qw.orderByDesc("create_time");
        return R.ok(sysOperationLogService.page(pageParam, qw));
    }
}
