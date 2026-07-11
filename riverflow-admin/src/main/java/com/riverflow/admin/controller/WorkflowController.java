package com.riverflow.admin.controller;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.riverflow.admin.infra.dynamicds.DynamicDataSourceService;
import com.riverflow.admin.infra.datascope.DataScope;
import com.riverflow.admin.modules.workflow.context.FlowContext;
import com.riverflow.admin.modules.workflow.engine.FlowEngine;
import com.riverflow.admin.modules.workflow.loop.LoopState;
import com.riverflow.admin.service.*;
import com.riverflow.api.entity.*;
import com.riverflow.api.enums.FlowInstanceStatusEnum;
import com.riverflow.api.enums.FlowNodeTypeEnum;
import com.riverflow.api.enums.FlowTaskStatusEnum;
import com.riverflow.api.enums.FlowTaskTypeEnum;
import com.riverflow.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    @Autowired
    private com.riverflow.admin.modules.workflow.loop.LoopValidator loopValidator;
    @Autowired
    private com.riverflow.admin.modules.workflow.validate.FlowValidator flowValidator;
    @Autowired
    private DynamicDataSourceService dynamicDataSourceService;

    // ==================== 流程定义 ====================

    @GetMapping("/definition/list")
    @PreAuthorize("@ss.hasPerm('workflow:list')")
    @DataScope(deptColumn = "dept_id", userColumn = "create_by")
    public R<Page<FlowDefinition>> listDefinitions(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String flowCode,
            @RequestParam(required = false) String flowName,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Boolean showAllVersions) {

        boolean allVersions = showAllVersions != null && showAllVersions;

        QueryWrapper<FlowDefinition> qw = new QueryWrapper<>();
        qw.eq("del_flag", 0);
        if (flowCode != null && !flowCode.isEmpty()) qw.eq("flow_code", flowCode);
        if (flowName != null && !flowName.isEmpty()) qw.like("flow_name", flowName);
        if (status != null) qw.eq("status", status);
        qw.orderByDesc("create_time");

        if (allVersions) {
            Page<FlowDefinition> pageParam = new Page<>(page, size);
            return R.ok(flowDefinitionService.page(pageParam, qw));
        }

        // 默认只显示每个 flow_code 的最新版本
        // 由于流程定义数据量通常不大，先全量查询再内存分页
        List<FlowDefinition> allList = flowDefinitionService.list(qw);
        Map<String, FlowDefinition> latestMap = new LinkedHashMap<>();
        for (FlowDefinition def : allList) {
            FlowDefinition existing = latestMap.get(def.getFlowCode());
            if (existing == null || (def.getVersion() != null && existing.getVersion() != null
                    && def.getVersion() > existing.getVersion())) {
                latestMap.put(def.getFlowCode(), def);
            }
        }
        List<FlowDefinition> latestList = new ArrayList<>(latestMap.values());
        latestList.sort(Comparator.comparing(FlowDefinition::getCreateTime).reversed());

        // 手动分页
        int total = latestList.size();
        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, total);
        List<FlowDefinition> records = fromIndex < total ? latestList.subList(fromIndex, toIndex) : new ArrayList<>();

        Page<FlowDefinition> resultPage = new Page<>(page, size, total);
        resultPage.setRecords(records);
        return R.ok(resultPage);
    }

    /**
     * 查询某流程编码的所有历史版本
     */
    @GetMapping("/definition/versions")
    @PreAuthorize("@ss.hasPerm('workflow:list')")
    public R<List<FlowDefinition>> listVersions(@RequestParam String flowCode) {
        QueryWrapper<FlowDefinition> qw = new QueryWrapper<>();
        qw.eq("flow_code", flowCode).eq("del_flag", 0).orderByDesc("version");
        return R.ok(flowDefinitionService.list(qw));
    }

    @GetMapping("/definition/{id}")
    @PreAuthorize("@ss.hasPerm('workflow:list')")
    public R<FlowDefinition> getDefinition(@PathVariable Long id) {
        FlowDefinition def = flowDefinitionService.getById(id);
        if (def == null) return R.fail("流程定义不存在");
        return R.ok(def);
    }

    @PostMapping("/definition")
    @PreAuthorize("@ss.hasPerm('workflow:add')")
    public R<String> saveDefinition(@RequestBody FlowDefinition definition) {
        if (definition.getId() != null) {
            FlowDefinition exist = flowDefinitionService.getById(definition.getId());
            if (exist != null && exist.getStatus() != null && exist.getStatus() == 1) {
                return R.fail("已发布的流程不可直接修改，请先创建新版本");
            }
            // 编辑已有流程时，若未传 version，保持原值，避免唯一键冲突
            if (definition.getVersion() == null && exist != null) {
                definition.setVersion(exist.getVersion());
            }
            // 编辑已有流程时，若未传字段，保留旧值
            if (exist != null) {
                if (definition.getInputParams() == null) {
                    definition.setInputParams(exist.getInputParams());
                }
                if (definition.getOutputParams() == null) {
                    definition.setOutputParams(exist.getOutputParams());
                }
                if (definition.getExecutionMode() == null || definition.getExecutionMode().isEmpty()) {
                    definition.setExecutionMode(exist.getExecutionMode());
                }
                if (definition.getTriggerType() == null || definition.getTriggerType().isEmpty()) {
                    definition.setTriggerType(exist.getTriggerType());
                }
                if (definition.getItemCode() == null || definition.getItemCode().isEmpty()) {
                    definition.setItemCode(exist.getItemCode());
                }
            }
        }
        if (definition.getVersion() == null) {
            definition.setVersion(1);
        }
        // 执行模式校验与默认值
        if (definition.getExecutionMode() == null || definition.getExecutionMode().isEmpty()) {
            definition.setExecutionMode("ASYNC");
        }
        if (!"ASYNC".equals(definition.getExecutionMode()) && !"SYNC".equals(definition.getExecutionMode())) {
            return R.fail("执行模式只能是 ASYNC 或 SYNC");
        }
        flowDefinitionService.saveOrUpdate(definition);
        return R.ok(String.valueOf(definition.getId()));
    }

    @PostMapping("/definition/{id}/validate")
    @PreAuthorize("@ss.hasPerm('workflow:add')")
    public R<Void> validateDefinition(@PathVariable Long id) {
        FlowDefinition def = flowDefinitionService.getById(id);
        if (def == null) return R.fail("流程定义不存在");
        List<FlowNode> nodes = flowNodeService.getNodesByFlowId(id);
        List<String> errors = flowValidator.validate(nodes);
        if (!errors.isEmpty()) {
            return R.fail(String.join("; ", errors));
        }
        return R.ok();
    }

    @PutMapping("/definition/{id}/publish")
    @PreAuthorize("@ss.hasPerm('workflow:publish')")
    @Transactional(rollbackFor = Exception.class)
    public R<String> publishDefinition(@PathVariable Long id) {
        FlowDefinition def = flowDefinitionService.getById(id);
        if (def == null) return R.fail("流程定义不存在");

        List<FlowNode> nodes = flowNodeService.getNodesByFlowId(id);

        // 同步流程发布前校验：不能包含 timer 节点
        if ("SYNC".equals(def.getExecutionMode())) {
            boolean hasTimer = nodes.stream().anyMatch(n -> "timer".equals(n.getNodeType()));
            if (hasTimer) {
                return R.fail("同步流程不能包含定时(timer)节点，请修改流程图后重新发布");
            }
        }

        // 发布前校验：DB 节点 SQL 占位符必须在输入映射中配置
        if (def.getStatus() == null || def.getStatus() != 1) {
            List<String> errors = flowValidator.validate(nodes);
            if (!errors.isEmpty()) {
                return R.fail(String.join("; ", errors));
            }
        }

        if (def.getStatus() != null && def.getStatus() == 1) {
            // 已经是已发布状态，直接返回当前ID
            return R.ok(String.valueOf(def.getId()));
        }

        if (def.getStatus() != null && def.getStatus() == 0) {
            // 草稿状态：直接发布（若版本号未设置则自动递增）
            if (def.getVersion() == null || def.getVersion() == 0) {
                Integer maxVersion = flowDefinitionService.getMaxVersion(def.getFlowCode());
                def.setVersion((maxVersion == null ? 0 : maxVersion) + 1);
            }
            def.setStatus(1);
            def.setUpdateTime(LocalDateTime.now());
            flowDefinitionService.updateById(def);
            // 将同流程的其他已发布版本下线，保证同一时刻只有一个已发布版本
            offlineOtherPublishedVersions(def.getFlowCode(), def.getId());
            return R.ok(String.valueOf(def.getId()));
        }

        // 下线状态(status=2) 或其他状态：复制为新版本再发布
        Long newId = flowDefinitionService.copyAsNewVersion(id);
        FlowDefinition newDef = flowDefinitionService.getById(newId);
        newDef.setStatus(1);
        newDef.setUpdateTime(LocalDateTime.now());
        flowDefinitionService.updateById(newDef);
        // 将同流程的其他已发布版本下线，保证同一时刻只有一个已发布版本
        offlineOtherPublishedVersions(newDef.getFlowCode(), newDef.getId());
        return R.ok(String.valueOf(newId));
    }

    @PutMapping("/definition/{id}/offline")
    @PreAuthorize("@ss.hasPerm('workflow:publish')")
    public R<Void> offlineDefinition(@PathVariable Long id) {
        FlowDefinition def = flowDefinitionService.getById(id);
        if (def == null) return R.fail("流程定义不存在");
        def.setStatus(2);
        flowDefinitionService.updateById(def);
        return R.ok();
    }

    /**
     * 将同流程编码下除指定版本外的其他已发布版本下线
     */
    private void offlineOtherPublishedVersions(String flowCode, Long excludeId) {
        com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<FlowDefinition> updateWrapper =
                new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<>();
        updateWrapper.eq("flow_code", flowCode)
                .eq("status", 1)
                .ne("id", excludeId)
                .set("status", 2)
                .set("update_time", LocalDateTime.now());
        flowDefinitionService.update(updateWrapper);
    }

    @DeleteMapping("/definition/{id}")
    @PreAuthorize("@ss.hasPerm('workflow:delete')")
    public R<Void> deleteDefinition(@PathVariable Long id) {
        FlowDefinition def = flowDefinitionService.getById(id);
        if (def == null) return R.fail("流程定义不存在");
        // 只允许删除草稿或已下线的版本
        if (def.getStatus() != null && def.getStatus() == 1) {
            return R.fail("已发布的流程版本不可删除");
        }
        def.setDelFlag(1);
        flowDefinitionService.updateById(def);
        return R.ok();
    }

    /**
     * 复制指定版本为新草稿
     */
    @PostMapping("/definition/{id}/copy")
    @PreAuthorize("@ss.hasPerm('workflow:add')")
    public R<String> copyAsNewVersion(@PathVariable Long id) {
        Long newId = flowDefinitionService.copyAsNewVersion(id);
        return R.ok(String.valueOf(newId));
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
    @PreAuthorize("@ss.hasPerm('workflow:edit')")
    @Transactional(rollbackFor = Exception.class)
    public R<Void> saveGraph(@PathVariable Long flowId, @RequestBody com.alibaba.fastjson2.JSONObject request) {
        FlowDefinition def = flowDefinitionService.getById(flowId);
        if (def == null) return R.fail("流程定义不存在");
        if (def.getStatus() != null && def.getStatus() == 1) {
            return R.fail("已发布的流程不可修改，请先创建新版本");
        }

        com.alibaba.fastjson2.JSONArray nodes = request.getJSONArray("nodes");

        // 循环结构校验
        List<FlowNode> nodeListForValidate = new ArrayList<>();
        List<FlowEdge> edgeListForValidate = new ArrayList<>();
        if (nodes != null) {
            for (int i = 0; i < nodes.size(); i++) {
                com.alibaba.fastjson2.JSONObject nodeJson = nodes.getJSONObject(i);
                FlowNode node = new FlowNode();
                node.setNodeId(nodeJson.getString("id"));
                node.setNodeName(nodeJson.getString("text"));
                node.setNodeType(nodeJson.getString("type"));
                node.setConfigJson(nodeJson.getJSONObject("properties") != null ?
                        nodeJson.getJSONObject("properties").toJSONString() : null);
                nodeListForValidate.add(node);
            }
        }
        com.alibaba.fastjson2.JSONArray edgesForValidate = request.getJSONArray("edges");
        if (edgesForValidate != null) {
            for (int i = 0; i < edgesForValidate.size(); i++) {
                com.alibaba.fastjson2.JSONObject edgeJson = edgesForValidate.getJSONObject(i);
                FlowEdge edge = new FlowEdge();
                edge.setSourceNode(edgeJson.getString("sourceNodeId"));
                edge.setTargetNode(edgeJson.getString("targetNodeId"));
                edgeListForValidate.add(edge);
            }
        }
        loopValidator.validate(nodeListForValidate, edgeListForValidate);

        // 同步流程校验：不能包含 timer 节点
        if ("SYNC".equals(def.getExecutionMode()) && nodes != null) {
            for (int i = 0; i < nodes.size(); i++) {
                com.alibaba.fastjson2.JSONObject nodeJson = nodes.getJSONObject(i);
                if ("timer".equals(nodeJson.getString("type"))) {
                    return R.fail("同步流程不支持定时(timer)节点，请删除后保存");
                }
            }
        }

        // 保存节点（物理删除旧记录，避免唯一键冲突）
        flowNodeService.physicalDeleteByFlowId(flowId);
        if (nodes != null && !nodes.isEmpty()) {
            List<FlowNode> nodeList = new ArrayList<>();
            for (int i = 0; i < nodes.size(); i++) {
                com.alibaba.fastjson2.JSONObject nodeJson = nodes.getJSONObject(i);
                FlowNode node = new FlowNode();
                node.setFlowId(flowId);
                node.setNodeId(nodeJson.getString("id"));
                node.setNodeName(nodeJson.getString("text"));
                node.setNodeType(nodeJson.getString("type"));

                // properties 整体序列化为 configJson
                com.alibaba.fastjson2.JSONObject props = nodeJson.getJSONObject("properties");
                if (props != null) {
                    node.setConfigJson(props.toJSONString());
                    if (node.getNodeName() == null) {
                        node.setNodeName(props.getString("name"));
                    }
                    // 提取输入/输出映射（前端以JSON字符串存储）
                    if (props.containsKey("inputMapping")) {
                        node.setInputMapping(props.getString("inputMapping"));
                    }
                    if (props.containsKey("outputMapping")) {
                        node.setOutputMapping(props.getString("outputMapping"));
                    }
                    // 提取超时和重试（兼容API/DB节点配置）
                    if (props.containsKey("timeout")) {
                        node.setTimeout(props.getIntValue("timeout"));
                    }
                    if (props.containsKey("retryTimes")) {
                        node.setRetryTimes(props.getIntValue("retryTimes"));
                    }
                }

                // x, y 坐标
                Object x = nodeJson.get("x");
                Object y = nodeJson.get("y");
                if (x != null) node.setXCoordinate(new java.math.BigDecimal(x.toString()));
                if (y != null) node.setYCoordinate(new java.math.BigDecimal(y.toString()));

                node.setSortNo(i);
                node.setDelFlag(0);
                nodeList.add(node);
            }
            // 去重：同一个 flow_id + node_id 只保留一条（防止前端数据异常）
            Map<String, FlowNode> nodeMap = new java.util.LinkedHashMap<>();
            for (FlowNode n : nodeList) {
                nodeMap.put(n.getNodeId(), n);
            }
            flowNodeService.saveBatch(new ArrayList<>(nodeMap.values()));
        }

        // 保存边（物理删除旧记录，避免唯一键冲突）
        flowEdgeService.physicalDeleteByFlowId(flowId);
        com.alibaba.fastjson2.JSONArray edges = request.getJSONArray("edges");
        if (edges != null && !edges.isEmpty()) {
            List<FlowEdge> edgeList = new ArrayList<>();
            for (int i = 0; i < edges.size(); i++) {
                com.alibaba.fastjson2.JSONObject edgeJson = edges.getJSONObject(i);
                FlowEdge edge = new FlowEdge();
                edge.setFlowId(flowId);
                edge.setEdgeId(edgeJson.getString("id"));
                edge.setSourceNode(edgeJson.getString("sourceNodeId"));
                edge.setTargetNode(edgeJson.getString("targetNodeId"));

                com.alibaba.fastjson2.JSONObject props = edgeJson.getJSONObject("properties");
                if (props != null) {
                    edge.setConditionType(props.getString("conditionType"));
                    edge.setConditionExpression(props.getString("conditionExpression"));
                }

                edge.setPriority(i);
                edge.setDelFlag(0);
                edgeList.add(edge);
            }
            flowEdgeService.saveBatch(edgeList);
        }

        return R.ok();
    }

    // ==================== 流程实例 ====================

    @GetMapping("/instance/list")
    @PreAuthorize("@ss.hasPerm('workflow:instance:list')")
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
    @PreAuthorize("@ss.hasPerm('workflow:instance:execute')")
    public R<String> startInstance(@PathVariable Long flowId,
                                  @RequestParam(required = false) String businessKey,
                                  @RequestParam(required = false) String itemCode) {
        FlowDefinition def = flowDefinitionService.getById(flowId);
        if (def == null) return R.fail("流程定义不存在");
        if (def.getStatus() != 1) return R.fail("流程未发布，无法启动");

        FlowInstance instance = flowEngine.startInstance(flowId, def.getFlowCode(), def.getVersion(), businessKey, itemCode);

        // 注入流程默认入参
        if (def.getInputParams() != null && !def.getInputParams().isEmpty()) {
            try {
                String existingContext = instance.getContextJson();
                Map<String, Object> contextMap;
                if (existingContext != null && !existingContext.isEmpty()) {
                    contextMap = com.alibaba.fastjson2.JSON.parseObject(existingContext, Map.class);
                } else {
                    contextMap = new HashMap<>();
                }
                Map<String, Object> defaultVars = com.alibaba.fastjson2.JSON.parseObject(def.getInputParams(), Map.class);
                if (defaultVars != null) {
                    contextMap.putAll(defaultVars);
                    instance.setContextJson(com.alibaba.fastjson2.JSON.toJSONString(contextMap));
                    flowInstanceService.updateById(instance);
                }
            } catch (Exception e) {
                log.warn("注入流程默认入参失败", e);
            }
        }

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

        return R.ok(String.valueOf(instance.getId()));
    }

    @PutMapping("/instance/{id}/terminate")
    @PreAuthorize("@ss.hasPerm('workflow:instance:execute')")
    public R<Void> terminateInstance(@PathVariable Long id) {
        FlowInstance instance = flowInstanceService.getById(id);
        if (instance == null) return R.fail("实例不存在");
        instance.setStatus(FlowInstanceStatusEnum.TERMINATED.getCode());
        instance.setEndTime(LocalDateTime.now());
        flowInstanceService.updateById(instance);
        return R.ok();
    }

    @PutMapping("/instance/{id}/suspend")
    @PreAuthorize("@ss.hasPerm('workflow:instance:execute')")
    public R<Void> suspendInstance(@PathVariable Long id) {
        FlowInstance instance = flowInstanceService.getById(id);
        if (instance == null) return R.fail("实例不存在");
        if (!FlowInstanceStatusEnum.RUNNING.getCode().equals(instance.getStatus())) {
            return R.fail("仅运行中的实例可手动挂起");
        }
        instance.setStatus(FlowInstanceStatusEnum.SUSPENDED.getCode());
        instance.setUpdateTime(LocalDateTime.now());
        flowInstanceService.updateById(instance);
        return R.ok();
    }

    @PostMapping("/instance/{instanceId}/resume")
    @PreAuthorize("@ss.hasPerm('workflow:instance:execute')")
    public R<String> resumeInstance(@PathVariable Long instanceId) {
        FlowInstance instance = flowInstanceService.getById(instanceId);
        if (instance == null) return R.fail("实例不存在");
        if (!FlowInstanceStatusEnum.SUSPENDED.getCode().equals(instance.getStatus())) {
            return R.fail("实例不是挂起状态，无法继续");
        }
        return doResumeOrRetry(instance, "继续执行");
    }

    @PostMapping("/instance/{instanceId}/retry")
    @PreAuthorize("@ss.hasPerm('workflow:instance:execute')")
    public R<String> retryInstance(@PathVariable Long instanceId) {
        FlowInstance instance = flowInstanceService.getById(instanceId);
        if (instance == null) return R.fail("实例不存在");
        if (!FlowInstanceStatusEnum.FAILED.getCode().equals(instance.getStatus())) {
            return R.fail("实例不是失败状态，无法重试");
        }
        return doResumeOrRetry(instance, "重试");
    }

    private R<String> doResumeOrRetry(FlowInstance instance, String actionName) {
        // 1. 恢复实例状态
        instance.setStatus(FlowInstanceStatusEnum.RUNNING.getCode());
        instance.setUpdateTime(LocalDateTime.now());
        flowInstanceService.updateById(instance);

        // 2. 获取流程图（始终使用实例绑定的版本定义）
        List<FlowNode> nodes = flowNodeService.getNodesByFlowId(instance.getFlowId());
        List<FlowEdge> edges = flowEdgeService.getEdgesByFlowId(instance.getFlowId());

        FlowNode currentNode = nodes.stream()
                .filter(n -> n.getNodeId().equals(instance.getCurrentNodeId()))
                .findFirst().orElse(null);
        if (currentNode == null) return R.fail("当前节点不存在");

        // 3. 将当前节点的失败任务重置为待执行（如果存在）
        FlowTask latestTask = flowTaskService.getOne(
                new QueryWrapper<FlowTask>()
                        .eq("instance_id", instance.getId())
                        .eq("node_id", currentNode.getNodeId())
                        .orderByDesc("create_time")
                        .last("LIMIT 1")
        );
        if (latestTask != null && FlowTaskStatusEnum.FAIL.getCode().equals(latestTask.getStatus())) {
            latestTask.setStatus(FlowTaskStatusEnum.PENDING.getCode());
            latestTask.setErrorMsg(null);
            latestTask.setEndTime(null);
            latestTask.setNextExecuteTime(null);
            flowTaskService.updateById(latestTask);
        }

        // 4. 触发执行
        flowEngine.executeNode(instance, currentNode, edges, nodes);
        return R.ok(actionName + "完成");
    }

    // ==================== 流程执行（手动推进）====================

    @PostMapping("/instance/{instanceId}/execute")
    @PreAuthorize("@ss.hasPerm('workflow:instance:execute')")
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

    /**
     * 查询循环节点执行进度
     */
    @GetMapping("/instance/{instanceId}/loop-progress")
    public R<Map<String, Object>> getLoopProgress(@PathVariable Long instanceId,
                                                   @RequestParam String loopNodeId) {
        FlowInstance instance = flowInstanceService.getById(instanceId);
        if (instance == null) {
            return R.fail("实例不存在");
        }

        FlowContext context = FlowContext.fromJson(instance.getContextJson());
        LoopState state = LoopState.from(context.getGlobal(LoopState.key(loopNodeId)));

        boolean isParallel = state != null && state.isParallel();
        int total = state != null ? state.getTotal() : 0;
        int currentIndex = state != null ? state.getIndex() : 0;

        List<FlowTask> iterationTasks;
        if (isParallel) {
            iterationTasks = flowTaskService.listByInstanceIdAndLoopNodeIdAndTaskType(
                    instanceId, loopNodeId, FlowTaskTypeEnum.LOOP_ITERATION.getCode());
        } else {
            // 串行模式：入口节点任务被合并复用，按 loop_node_id 查询即可
            iterationTasks = flowTaskService.list(
                    new QueryWrapper<FlowTask>()
                            .eq("instance_id", instanceId)
                            .eq("loop_node_id", loopNodeId)
                            .orderByAsc("create_time")
            );
        }

        long completedCount = 0;
        long failedCount = 0;
        long runningCount = 0;
        long pendingCount = 0;
        for (FlowTask task : iterationTasks) {
            String status = task.getStatus();
            if (FlowTaskStatusEnum.SUCCESS.getCode().equals(status)) {
                completedCount++;
            } else if (FlowTaskStatusEnum.FAIL.getCode().equals(status)) {
                failedCount++;
            } else if (FlowTaskStatusEnum.RUNNING.getCode().equals(status)) {
                runningCount++;
            } else if (FlowTaskStatusEnum.PENDING.getCode().equals(status)) {
                pendingCount++;
            }
        }
        if (!isParallel && state != null) {
            // 串行进度以 LoopState 为准
            completedCount = Math.min(state.getResults() != null ? state.getResults().size() : 0, total);
        }
        int progress = total > 0 ? (int) ((completedCount * 100) / total) : 0;

        Map<String, Object> result = new HashMap<>();
        result.put("loopNodeId", loopNodeId);
        result.put("parallel", isParallel);
        result.put("total", total);
        result.put("currentIndex", currentIndex);
        result.put("progress", progress);
        result.put("completed", completedCount);
        result.put("failed", failedCount);
        result.put("running", runningCount);
        result.put("pending", pendingCount);
        result.put("batchNo", state != null ? state.getBatchNo() : null);

        List<Map<String, Object>> iterations = new ArrayList<>();
        for (FlowTask task : iterationTasks) {
            Map<String, Object> item = new HashMap<>();
            item.put("taskId", task.getId());
            item.put("taskType", task.getTaskType());
            item.put("iterationIndex", task.getIterationIndex());
            item.put("nodeId", task.getNodeId());
            item.put("nodeName", task.getNodeName());
            item.put("status", task.getStatus());
            item.put("executeCount", task.getExecuteCount());
            item.put("startTime", task.getStartTime());
            item.put("endTime", task.getEndTime());
            item.put("errorMsg", task.getErrorMsg());
            iterations.add(item);
        }
        result.put("iterations", iterations);

        return R.ok(result);
    }

    @GetMapping("/instance/{instanceId}/logs")
    public R<Page<FlowLog>> getInstanceLogs(
            @PathVariable Long instanceId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "5") Integer size) {
        Page<FlowLog> pageParam = new Page<>(page, size);
        QueryWrapper<FlowLog> qw = new QueryWrapper<>();
        qw.eq("instance_id", instanceId);
        qw.eq("del_flag", 0);
        qw.orderByDesc("create_time");
        qw.orderByDesc("id");
        return R.ok(flowLogService.page(pageParam, qw));
    }

    /**
     * 解析SQL语句的返回字段列表
     * 用于前端DB节点配置时，自动提取SELECT返回的列名
     */
    @PostMapping("/node/parse-sql-columns")
    public R<List<Map<String, String>>> parseSqlColumns(@RequestBody com.alibaba.fastjson2.JSONObject request) {
        String dsCode = request.getString("dsCode");
        String sql = request.getString("sql");
        if (sql == null || sql.trim().isEmpty()) {
            return R.fail("SQL不能为空");
        }

        // 替换 #{xxx} SpEL 占位符，避免解析执行时报错
        String testSql = sql.replaceAll("#\\{[^}]+}", "'__placeholder__'");

        DataSource dataSource = dynamicDataSourceService.resolveDataSource(dsCode);

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(testSql)) {

            ResultSetMetaData meta = null;
            try {
                meta = ps.getMetaData();
            } catch (Exception e) {
                log.debug("PreparedStatement.getMetaData() 不支持，退回到 executeQuery");
            }

            if (meta == null) {
                try (ResultSet rs = ps.executeQuery()) {
                    meta = rs.getMetaData();
                }
            }

            List<Map<String, String>> columns = new ArrayList<>();
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                Map<String, String> col = new LinkedHashMap<>();
                col.put("name", meta.getColumnLabel(i));      // 列别名（或列名）
                col.put("dbName", meta.getColumnName(i));     // 数据库原始列名
                col.put("type", meta.getColumnTypeName(i));   // 数据类型
                columns.add(col);
            }
            return R.ok(columns);
        } catch (Exception e) {
            log.error("SQL字段解析失败: {}", testSql, e);
            return R.fail("SQL字段解析失败: " + e.getMessage());
        }
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
