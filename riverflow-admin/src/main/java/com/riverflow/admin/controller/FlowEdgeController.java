package com.riverflow.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.riverflow.admin.service.FlowEdgeService;
import com.riverflow.api.entity.FlowEdge;
import com.riverflow.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 流程边（连线）管理 Controller
 */
@Slf4j
@RestController
@RequestMapping("/flow-edge")
public class FlowEdgeController {

    @Autowired
    private FlowEdgeService flowEdgeService;

    @GetMapping("/list")
    public R<Page<FlowEdge>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long flowId,
            @RequestParam(required = false) String conditionType) {
        Page<FlowEdge> pageParam = new Page<>(page, size);
        QueryWrapper<FlowEdge> qw = new QueryWrapper<>();
        qw.eq("del_flag", 0);
        if (flowId != null) qw.eq("flow_id", flowId);
        if (conditionType != null && !conditionType.isEmpty()) qw.eq("condition_type", conditionType);
        qw.orderByAsc("priority");
        return R.ok(flowEdgeService.page(pageParam, qw));
    }

    @GetMapping("/{id}")
    public R<FlowEdge> getById(@PathVariable Long id) {
        FlowEdge edge = flowEdgeService.getById(id);
        if (edge == null) return R.fail("边不存在");
        return R.ok(edge);
    }

    @PostMapping
    public R<Long> save(@RequestBody FlowEdge edge) {
        flowEdgeService.saveOrUpdate(edge);
        return R.ok(edge.getId());
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        FlowEdge edge = new FlowEdge();
        edge.setId(id);
        edge.setDelFlag(1);
        flowEdgeService.updateById(edge);
        return R.ok();
    }

    @GetMapping("/flow/{flowId}")
    public R<List<FlowEdge>> getByFlowId(@PathVariable Long flowId) {
        return R.ok(flowEdgeService.getEdgesByFlowId(flowId));
    }
}
