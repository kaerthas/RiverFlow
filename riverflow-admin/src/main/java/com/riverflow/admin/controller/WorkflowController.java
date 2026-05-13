package com.riverflow.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.riverflow.admin.modules.workflow.context.FlowContext;
import com.riverflow.admin.modules.workflow.engine.FlowEngine;
import com.riverflow.admin.service.*;
import com.riverflow.api.entity.*;
import com.riverflow.api.enums.FlowInstanceStatusEnum;
import com.riverflow.api.enums.FlowNodeTypeEnum;
import com.riverflow.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 工作流管理 Controller
 */
@Slf4j
@RestController
@RequestMapping("/workflow")
public class WorkflowController {

    @Autowired
    private FlowDefinitionService flowDefinitionService;
    @Autowired
    private FlowInstanceService flowInstanceService;
    @Autowired
    private FlowTaskService flowTaskService;
    @Autowired
    private FlowNodeService flowNodeService;
    @Autowired
    private FlowEdgeService flowEdgeService;
    @Autowired
    private FlowLogService flowLogService;
    @Autowired
    private FlowEngine flowEngine;

    // ==================== 流程定义 ====================

    @GetMapping("/definition/list")
    public R<Page<FlowDefinition>> listDefinitions(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String flowCode,
            @RequestParam(required = false) String flowName) {
        Page<FlowDefinition> pageParam = new Page<>(page, size);
        QueryWrapper<FlowDefinition> qw = new QueryWrapper<>();
        qw.eq("del_flag", 0);
        if (flowCode != null && !flowCode.isEmpty()) qw.like("flow_code", flowCode);
        if (flowName != null && !flowName.isEmpty()) qw.like("flow_name", flowName);
        qw.orderByDesc("create_time");
        return R.ok(flowDefinitionService.page(pageParam, qw));
    }

    @GetMapping("/definition/{id}")
    public R<FlowDefinition> getDefinition(@PathVariable Long id) {
        FlowDefinition def = flowDefinitionService.getById(id);
        if (def == null) return R.fail("流程定义不存在");
        return R.ok(def);
    }

    @PostMapping("/definition")
    public R<Long> saveDefinition(@RequestBody FlowDefinition definition) {
        flowDefinitionService.saveOrUpdate(definition);
        return R.ok(definition.getId());
    }

    @PutMapping("/definition/{id}/publish")
    public R<Void> publishDefinition(@PathVariable Long id) {
        FlowDefinition def = flowDefinitionService.getById(id);
        if (def == null) return R.fail("流程定义不存在");
        def.setStatus(1);
        flowDefinitionService.updateById(def);
        return R.ok();
    }

    @PutMapping("/definition/{id}/offline")
    public R<Void> offlineDefinition(@PathVariable Long id) {
        FlowDefinition def = flowDefinitionService.getById(id);
        if (def == null) return R.fail("流程定义不存在");
        def.setStatus(2);
        flowDefinitionService.updateById(def);
        return R.ok();
    }

    @DeleteMapping("/definition/{id}")
    public R<Void> deleteDefinition(@PathVariable Long id) {
        FlowDefinition def = new FlowDefinition();
        def.setId(id);
        def.setDelFlag(1);
        flowDefinitionService.updateById(def);
        return R.ok();
    }

    // ==================== 流程节点与边 ====================

    @GetMapping("/definition/{flowId}/nodes")
    public R<List<FlowNode>> getNodes(@PathVariable Long flowId) {
        return R.ok(flowNodeService.getNodesByFlowId(flowId));
    }

    @GetMapping("/definition/{flowId}/edges")
    public R<List<FlowEdge>> getEdges(@PathVariable Long flowId) {
        return R.ok(flowEdgeService.getEdgesByFlowId(flowId));
    }

    @PostMapping("/definition/{flowId}/save-graph")
    public R<Void> saveGraph(@PathVariable Long flowId, @RequestBody GraphSaveRequest request) {
        FlowDefinition def = flowDefinitionService.getById(flowId);
        if (def == null) return R.fail("流程定义不存在");
        def.setGraphJson(request.getGraphJson());
        flowDefinitionService.updateById(def);

        // 保存节点
        flowNodeService.remove(new QueryWrapper<FlowNode>().eq("flow_id", flowId));
        if (request.getNodes() != null) {
            for (FlowNode node : request.getNodes()) {
                node.setFlowId(flowId);
                node.setDelFlag(0);
            }
            flowNodeService.saveBatch(request.getNodes());
        }

        // 保存边
        flowEdgeService.remove(new QueryWrapper<FlowEdge>().eq("flow_id", flowId));
        if (request.getEdges() != null) {
            for (FlowEdge edge : request.getEdges()) {
                edge.setFlowId(flowId);
                edge.setDelFlag(0);
            }
            flowEdgeService.saveBatch(request.getEdges());
        }

        return R.ok();
    }

    // ==================== 流程实例 ====================

    @GetMapping("/instance/list")
    public R<Page<FlowInstance>> listInstances(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String flowCode,
            @RequestParam(required = false) String status) {
        Page<FlowInstance> pageParam = new Page<>(page, size);
        QueryWrapper<FlowInstance> qw = new QueryWrapper<>();
        if (flowCode != null && !flowCode.isEmpty()) qw.eq("flow_code", flowCode);
        if (status != null && !status.isEmpty()) qw.eq("status", status);
        qw.orderByDesc("create_time");
        return R.ok(flowInstanceService.page(pageParam, qw));
    }

    @GetMapping("/instance/{id}")
    public R<FlowInstance> getInstance(@PathVariable Long id) {
        return R.ok(flowInstanceService.getById(id));
    }

    @PostMapping("/instance/{flowId}/start")
    public R<Long> startInstance(@PathVariable Long flowId,
                                  @RequestParam(required = false) String businessKey,
                                  @RequestParam(required = false) String itemCode) {
        FlowDefinition def = flowDefinitionService.getById(flowId);
        if (def == null) return R.fail("流程定义不存在");
        if (def.getStatus() != 1) return R.fail("流程未发布，无法启动");

        FlowInstance instance = flowEngine.startInstance(flowId, def.getFlowCode(), businessKey, itemCode);

        // 找到开始节点，创建首个任务
        List<FlowNode> nodes = flowNodeService.getNodesByFlowId(flowId);
        FlowNode startNode = nodes.stream()
                .filter(n -> FlowNodeTypeEnum.START.getCode().equals(n.getNodeType()))
                .findFirst().orElse(null);

        if (startNode != null) {
            FlowTask task = new FlowTask();
            task.setInstanceId(instance.getId());
            task.setNodeId(startNode.getNodeId());
            task.setNodeName(startNode.getNodeName());
            task.setNodeType(startNode.getNodeType());
            task.setStatus("pending");
            task.setCreateTime(LocalDateTime.now());
            flowTaskService.save(task);

            instance.setCurrentNodeId(startNode.getNodeId());
            flowInstanceService.updateById(instance);
        }

        return R.ok(instance.getId());
    }

    @PutMapping("/instance/{id}/terminate")
    public R<Void> terminateInstance(@PathVariable Long id) {
        FlowInstance instance = flowInstanceService.getById(id);
        if (instance == null) return R.fail("实例不存在");
        instance.setStatus(FlowInstanceStatusEnum.TERMINATED.getCode());
        instance.setEndTime(LocalDateTime.now());
        flowInstanceService.updateById(instance);
        return R.ok();
    }

    // ==================== 流程执行（手动推进）====================

    @PostMapping("/instance/{instanceId}/execute")
    public R<String> executeInstance(@PathVariable Long instanceId) {
        FlowInstance instance = flowInstanceService.getById(instanceId);
        if (instance == null) return R.fail("实例不存在");

        List<FlowNode> nodes = flowNodeService.getNodesByFlowId(instance.getFlowId());
        List<FlowEdge> edges = flowEdgeService.getEdgesByFlowId(instance.getFlowId());

        FlowNode currentNode = nodes.stream()
                .filter(n -> n.getNodeId().equals(instance.getCurrentNodeId()))
                .findFirst().orElse(null);

        if (currentNode == null) return R.fail("当前节点不存在");

        flowEngine.executeNode(instance, currentNode, edges, nodes);
        return R.ok("执行完成");
    }

    @GetMapping("/instance/{instanceId}/tasks")
    public R<List<FlowTask>> getInstanceTasks(@PathVariable Long instanceId) {
        return R.ok(flowTaskService.list(new QueryWrapper<FlowTask>()
                .eq("instance_id", instanceId).orderByAsc("create_time")));
    }

    @GetMapping("/instance/{instanceId}/logs")
    public R<List<FlowLog>> getInstanceLogs(@PathVariable Long instanceId) {
        return R.ok(flowLogService.list(new QueryWrapper<FlowLog>()
                .eq("instance_id", instanceId).orderByDesc("create_time")));
    }

    /**
     * 图保存请求
     */
    @lombok.Data
    public static class GraphSaveRequest {
        private String graphJson;
        private List<FlowNode> nodes;
        private List<FlowEdge> edges;
    }
}
