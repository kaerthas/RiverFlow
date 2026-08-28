package com.riverflow.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.riverflow.admin.service.FlowNodeService;
import com.riverflow.api.entity.FlowNode;
import com.riverflow.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 流程节点管理 Controller
 */
@Slf4j
@RestController
@RequestMapping("/flow-node")
public class FlowNodeController {

    @Autowired
    private FlowNodeService flowNodeService;

    @GetMapping("/list")
    public R<Page<FlowNode>> list(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "flowId", required = false) Long flowId,
            @RequestParam(value = "nodeType", required = false) String nodeType,
            @RequestParam(value = "nodeName", required = false) String nodeName) {
        Page<FlowNode> pageParam = new Page<>(page, size);
        QueryWrapper<FlowNode> qw = new QueryWrapper<>();
        qw.eq("del_flag", 0);
        if (flowId != null) qw.eq("flow_id", flowId);
        if (nodeType != null && !nodeType.isEmpty()) qw.eq("node_type", nodeType);
        if (nodeName != null && !nodeName.isEmpty()) qw.like("node_name", nodeName);
        qw.orderByAsc("sort_no");
        return R.ok(flowNodeService.page(pageParam, qw));
    }

    @GetMapping("/{id}")
    public R<FlowNode> getById(@PathVariable Long id) {
        FlowNode node = flowNodeService.getById(id);
        if (node == null) return R.fail("节点不存在");
        return R.ok(node);
    }

    @PostMapping
    public R<Long> save(@RequestBody FlowNode node) {
        flowNodeService.saveOrUpdate(node);
        return R.ok(node.getId());
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        FlowNode node = new FlowNode();
        node.setId(id);
        node.setDelFlag(1);
        flowNodeService.updateById(node);
        return R.ok();
    }

    @GetMapping("/flow/{flowId}")
    public R<List<FlowNode>> getByFlowId(@PathVariable Long flowId) {
        return R.ok(flowNodeService.getNodesByFlowId(flowId));
    }
}
