package com.riverflow.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.riverflow.admin.service.FlowTaskService;
import com.riverflow.api.entity.FlowTask;
import com.riverflow.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 流程任务管理 Controller
 */
@Slf4j
@RestController
@RequestMapping("/flow-task")
public class FlowTaskController {

    @Autowired
    private FlowTaskService flowTaskService;

    @GetMapping("/list")
    public R<Page<FlowTask>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long instanceId,
            @RequestParam(required = false) String nodeName,
            @RequestParam(required = false) String status) {
        Page<FlowTask> pageParam = new Page<>(page, size);
        QueryWrapper<FlowTask> qw = new QueryWrapper<>();
        qw.eq("del_flag", 0);
        if (instanceId != null) qw.eq("instance_id", instanceId);
        if (nodeName != null && !nodeName.isEmpty()) qw.like("node_name", nodeName);
        if (status != null && !status.isEmpty()) qw.eq("status", status);
        qw.orderByDesc("create_time");
        return R.ok(flowTaskService.page(pageParam, qw));
    }

    @GetMapping("/{id}")
    public R<FlowTask> getById(@PathVariable Long id) {
        FlowTask task = flowTaskService.getById(id);
        if (task == null) return R.fail("任务不存在");
        return R.ok(task);
    }

    @PostMapping
    public R<Long> save(@RequestBody FlowTask task) {
        flowTaskService.saveOrUpdate(task);
        return R.ok(task.getId());
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        FlowTask task = new FlowTask();
        task.setId(id);
        task.setDelFlag(1);
        flowTaskService.updateById(task);
        return R.ok();
    }

    @GetMapping("/instance/{instanceId}")
    public R<List<FlowTask>> getByInstanceId(@PathVariable Long instanceId) {
        return R.ok(flowTaskService.list(
                new QueryWrapper<FlowTask>()
                        .eq("instance_id", instanceId)
                        .eq("del_flag", 0)
                        .orderByAsc("create_time")));
    }
}
