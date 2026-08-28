package com.riverflow.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.riverflow.admin.service.*;
import com.riverflow.api.entity.ApiApp;
import com.riverflow.api.entity.ApiCallLog;
import com.riverflow.api.entity.ApiCatalog;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
    @Autowired
    private ApiCatalogService apiCatalogService;
    @Autowired
    private ApiAppService apiAppService;
    @Autowired
    private ApiCallLogService apiCallLogService;

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

    /**
     * 数据大盘总览：注册接口数、接入应用数、接口调用量
     */
    @GetMapping("/overview")
    public R<Map<String, Object>> overview() {
        Map<String, Object> result = new HashMap<>();

        long apiCount = apiCatalogService.count(new QueryWrapper<ApiCatalog>().eq("del_flag", 0));
        long appCount = apiAppService.count(new QueryWrapper<ApiApp>().eq("del_flag", 0));
        long callTotal = apiCallLogService.count(new QueryWrapper<ApiCallLog>().eq("del_flag", 0));
        long callToday = apiCallLogService.count(new QueryWrapper<ApiCallLog>()
                .eq("del_flag", 0)
                .ge("create_time", LocalDate.now().atStartOfDay()));
        long callFailed = apiCallLogService.count(new QueryWrapper<ApiCallLog>()
                .eq("del_flag", 0)
                .eq("call_status", 0));

        result.put("apiCount", apiCount);
        result.put("appCount", appCount);
        result.put("callTotal", callTotal);
        result.put("callToday", callToday);
        result.put("callFailed", callFailed);

        return R.ok(result);
    }

    /**
     * 近7天接口调用趋势
     */
    @GetMapping("/call-trend")
    public R<List<Map<String, Object>>> callTrend() {
        List<Map<String, Object>> list = new ArrayList<>();
        LocalDate today = LocalDate.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");
        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            long count = apiCallLogService.count(new QueryWrapper<ApiCallLog>()
                    .eq("del_flag", 0)
                    .ge("create_time", day.atStartOfDay())
                    .lt("create_time", day.plusDays(1).atStartOfDay()));
            Map<String, Object> item = new HashMap<>();
            item.put("date", day.format(fmt));
            item.put("count", count);
            list.add(item);
        }
        return R.ok(list);
    }

    /**
     * 最新接口调用记录
     */
    @GetMapping("/recent-calls")
    public R<List<ApiCallLog>> recentCalls(@RequestParam(defaultValue = "10") Integer limit) {
        List<ApiCallLog> logs = apiCallLogService.list(
                new QueryWrapper<ApiCallLog>()
                        .select(ApiCallLog.class, field -> !"request_headers".equals(field.getColumn())
                                && !"request_body".equals(field.getColumn())
                                && !"response_body".equals(field.getColumn()))
                        .eq("del_flag", 0)
                        .orderByDesc("create_time")
                        .last("LIMIT " + limit));
        return R.ok(logs);
    }
}
