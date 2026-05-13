package com.riverflow.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.riverflow.admin.service.FlowLogService;
import com.riverflow.api.entity.FlowLog;
import com.riverflow.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 流程日志管理 Controller
 */
@Slf4j
@RestController
@RequestMapping("/flow-log")
public class FlowLogController {

    @Autowired
    private FlowLogService flowLogService;

    @GetMapping("/list")
    public R<Page<FlowLog>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long instanceId,
            @RequestParam(required = false) String logType,
            @RequestParam(required = false) String nodeId) {
        Page<FlowLog> pageParam = new Page<>(page, size);
        QueryWrapper<FlowLog> qw = new QueryWrapper<>();
        qw.eq("del_flag", 0);
        if (instanceId != null) qw.eq("instance_id", instanceId);
        if (logType != null && !logType.isEmpty()) qw.eq("log_type", logType);
        if (nodeId != null && !nodeId.isEmpty()) qw.eq("node_id", nodeId);
        qw.orderByDesc("create_time");
        return R.ok(flowLogService.page(pageParam, qw));
    }

    @GetMapping("/{id}")
    public R<FlowLog> getById(@PathVariable Long id) {
        FlowLog logEntry = flowLogService.getById(id);
        if (logEntry == null) return R.fail("日志不存在");
        return R.ok(logEntry);
    }

    @PostMapping
    public R<Long> save(@RequestBody FlowLog logEntry) {
        flowLogService.saveOrUpdate(logEntry);
        return R.ok(logEntry.getId());
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        FlowLog logEntry = new FlowLog();
        logEntry.setId(id);
        logEntry.setDelFlag(1);
        flowLogService.updateById(logEntry);
        return R.ok();
    }

    @GetMapping("/instance/{instanceId}")
    public R<List<FlowLog>> getByInstanceId(@PathVariable Long instanceId) {
        return R.ok(flowLogService.list(
                new QueryWrapper<FlowLog>()
                        .eq("instance_id", instanceId)
                        .eq("del_flag", 0)
                        .orderByDesc("create_time")));
    }
}
