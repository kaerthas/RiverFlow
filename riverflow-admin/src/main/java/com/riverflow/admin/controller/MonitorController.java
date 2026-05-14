package com.riverflow.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.riverflow.admin.service.*;
import com.riverflow.api.entity.FlowInstance;
import com.riverflow.api.entity.FlowLog;
import com.riverflow.api.entity.FlowTask;
import com.riverflow.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 运行监控 Controller
 */
@Slf4j
@RestController
@RequestMapping("/monitor")
public class MonitorController {

    @Autowired
    private FlowInstanceService flowInstanceService;
    @Autowired
    private FlowLogService flowLogService;
    @Autowired
    private FlowTaskService flowTaskService;

    /**
     * 实例统计
     */
    @GetMapping("/stats")
    public R<Map<String, Object>> stats() {
        Map<String, Object> result = new HashMap<>();

        long total = flowInstanceService.count();
        long running = flowInstanceService.count(new QueryWrapper<FlowInstance>().eq("status", "running"));
        long completed = flowInstanceService.count(new QueryWrapper<FlowInstance>().eq("status", "completed"));
        long failed = flowInstanceService.count(new QueryWrapper<FlowInstance>().eq("status", "failed"));

        result.put("total", total);
        result.put("running", running);
        result.put("completed", completed);
        result.put("failed", failed);

        return R.ok(result);
    }

    /**
     * 最近日志
     */
    @GetMapping("/recent-logs")
    public R<List<FlowLog>> recentLogs(@RequestParam(defaultValue = "20") Integer limit) {
        List<FlowLog> logs = flowLogService.list(
                new QueryWrapper<FlowLog>()
                        .eq("del_flag", 0)
                        .orderByDesc("create_time")
                        .last("LIMIT " + limit));
        return R.ok(logs);
    }

    /**
     * 待执行任务数
     */
    @GetMapping("/pending-tasks")
    public R<Long> pendingTasks() {
        long count = flowTaskService.count(
                new QueryWrapper<FlowTask>()
                        .eq("status", "pending")
                        .eq("del_flag", 0));
        return R.ok(count);
    }
}
