package com.riverflow.admin.modules.workflow.engine;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.riverflow.admin.modules.workflow.context.FlowContext;
import com.riverflow.admin.modules.workflow.node.NodeExecutor;
import com.riverflow.admin.modules.workflow.node.NodeExecutorFactory;
import com.riverflow.admin.service.FlowEdgeService;
import com.riverflow.admin.service.FlowInstanceService;
import com.riverflow.admin.service.FlowLogService;
import com.riverflow.admin.service.FlowNodeService;
import com.riverflow.admin.service.FlowTaskService;
import com.riverflow.api.entity.FlowEdge;
import com.riverflow.api.entity.FlowInstance;
import com.riverflow.api.entity.FlowLog;
import com.riverflow.api.entity.FlowNode;
import com.riverflow.api.entity.FlowTask;
import com.riverflow.api.enums.FlowInstanceStatusEnum;
import com.riverflow.api.enums.FlowNodeTypeEnum;
import com.riverflow.api.enums.FlowTaskStatusEnum;
import com.riverflow.common.constant.CommonConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 流程执行引擎
 */
@Slf4j
@Component
public class FlowEngine {

    @Autowired
    private NodeExecutorFactory nodeExecutorFactory;
    @Autowired
    private TransitionEngine transitionEngine;
    @Autowired
    private FlowInstanceService flowInstanceService;
    @Autowired
    private FlowTaskService flowTaskService;
    @Autowired
    private FlowLogService flowLogService;
    @Autowired
    private AsyncLogService asyncLogService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private FlowNodeService flowNodeService;
    @Autowired
    private FlowEdgeService flowEdgeService;

    /**
     * 启动流程实例（优化：只保存一次）
     */
    public FlowInstance startInstance(Long flowId, String flowCode, Integer version, String businessKey, String itemCode) {
        log.info("启动流程实例: flowCode={}, version={}, businessKey={}", flowCode, version, businessKey);

        FlowInstance instance = new FlowInstance();
        instance.setFlowId(flowId);
        instance.setFlowCode(flowCode);
        instance.setVersion(version);
        instance.setBusinessKey(businessKey);
        instance.setStatus(FlowInstanceStatusEnum.RUNNING.getCode());
        instance.setStartTime(LocalDateTime.now());
        instance.setCreateTime(LocalDateTime.now());
        instance.setUpdateTime(LocalDateTime.now());

        FlowContext context = new FlowContext();
        context.set("_businessKey", businessKey);
        context.set("_flowCode", flowCode);
        if (itemCode != null) {
            context.set("itemCode", itemCode);
        }
        instance.setContextJson(context.toJsonString());
        instance.setCurrentNodeId("");

        flowInstanceService.save(instance);

        context.set("_instanceId", instance.getId());

        saveLog(instance.getId(), null, null, "start", "流程实例启动成功, version=" + version);
        return instance;
    }

    /**
     * 执行流程实例的当前节点
     */
    @Transactional(rollbackFor = Exception.class)
    public void executeNode(FlowInstance instance, FlowNode node, List<FlowEdge> edges, List<FlowNode> nodes) {
        // 动态锁过期：节点超时 + 10秒缓冲，最少30秒
        int nodeTimeout = node.getTimeout() != null ? node.getTimeout() : 30000;
        int lockSeconds = Math.max(30, nodeTimeout / 1000 + 10);

        String lockKey = CommonConstant.FLOW_LOCK_PREFIX + instance.getId();
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", lockSeconds, TimeUnit.SECONDS);
        if (!Boolean.TRUE.equals(locked)) {
            log.warn("[流程实例:{}] 获取分布式锁失败，跳过本次执行", instance.getId());
            return;
        }

        try {
            // 二次校验：获取锁后，再次查询数据库确认实例状态和任务状态
            // 防止 Redis 锁过期后，其他线程已执行完该节点并流转到下一节点，导致重复执行
            FlowInstance freshInstance = flowInstanceService.getById(instance.getId());
            if (freshInstance == null || !FlowInstanceStatusEnum.RUNNING.getCode().equals(freshInstance.getStatus())) {
                log.warn("[流程实例:{}] 二次校验失败，实例状态不是运行中（status={}），释放锁并跳过",
                        instance.getId(), freshInstance != null ? freshInstance.getStatus() : "null");
                return;
            }

            FlowTask latestTask = flowTaskService.getOne(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<FlowTask>()
                            .eq("instance_id", instance.getId())
                            .eq("node_id", node.getNodeId())
                            .orderByDesc("create_time")
                            .last("LIMIT 1")
            );
            if (latestTask != null
                    && !FlowTaskStatusEnum.PENDING.getCode().equals(latestTask.getStatus())
                    && !FlowTaskStatusEnum.WAITING.getCode().equals(latestTask.getStatus())) {
                log.warn("[流程实例:{}] 二次校验失败，任务已被其他线程执行（status={}），释放锁并跳过",
                        instance.getId(), latestTask.getStatus());
                return;
            }

            FlowContext context = FlowContext.fromJson(instance.getContextJson());
            if (context.getInstanceId() == null) {
                context.set("_instanceId", instance.getId());
                context.set("_businessKey", instance.getBusinessKey());
                context.set("_flowCode", instance.getFlowCode());
            }

            log.info("[流程实例:{}] 开始执行节点: {} (type={})",
                    instance.getId(), node.getNodeName(), node.getNodeType());

            FlowTask task = getOrCreateTask(instance, node);

            // 旧实例兼容：timer 节点已被 FlowScheduler 唤醒（WAITING 且 next_execute_time 已到达）
            // 但上下文中没有 _timerTargetTime_，说明是旧代码产生的任务，直接完成并继续流转
            if ("timer".equals(node.getNodeType())
                    && FlowTaskStatusEnum.WAITING.getCode().equals(task.getStatus())
                    && context.get("_timerTargetTime_" + node.getNodeId()) == null) {
                log.info("[流程实例:{}] timer 节点旧实例兼容，直接完成", instance.getId());
                task.setStatus(FlowTaskStatusEnum.SUCCESS.getCode());
                task.setEndTime(LocalDateTime.now());
                task.setExecuteCount(task.getExecuteCount() + 1);
                flowTaskService.updateById(task);
                saveLog(instance.getId(), task.getId(), node.getNodeId(), "execute", "定时节点完成（旧实例兼容）");
                NodeExecuteResult result = NodeExecuteResult.success(new JSONObject());
                transitionEngine.transition(instance, node, edges, nodes, context, result);
                instance.setContextJson(context.toJsonString());
                flowInstanceService.updateById(instance);
                return;
            }

            task.setStatus(FlowTaskStatusEnum.RUNNING.getCode());
            task.setStartTime(LocalDateTime.now());
            task.setInputContext(context.toJsonString());
            task.setExecuteCount(task.getExecuteCount() + 1);
            flowTaskService.updateById(task);

            NodeExecutor executor = nodeExecutorFactory.getExecutor(node.getNodeType());
            NodeExecuteResult result = executor.execute(node, context);

            if (result.isSuccess()) {
                if (result.isWaiting()) {
                    handleWaiting(task, result);
                } else {
                    if (result.getData() != null) {
                        context.set("nodeResult_" + node.getNodeId(), result.getData());
                    }
                    task.setOutputContext(context.toJsonString());
                    task.setResultJson(JSON.toJSONString(result.getData()));
                    task.setStatus(FlowTaskStatusEnum.SUCCESS.getCode());
                    task.setEndTime(LocalDateTime.now());
                    flowTaskService.updateById(task);

                    instance.setContextJson(context.toJsonString());
                    flowInstanceService.updateById(instance);

                    saveLog(instance.getId(), task.getId(), node.getNodeId(), "execute",
                            "节点执行成功: " + node.getNodeName());

                    transitionEngine.transition(instance, node, edges, nodes, context, result);
                }
            } else {
                if ("skip".equals(node.getFailStrategy())) {
                    log.info("[流程实例:{}] 节点 {} 执行失败但策略为跳过，继续流转: {}",
                            instance.getId(), node.getNodeName(), result.getErrorMsg());
                    task.setStatus(FlowTaskStatusEnum.FAIL.getCode());
                    task.setErrorMsg(result.getErrorMsg());
                    task.setEndTime(LocalDateTime.now());
                    flowTaskService.updateById(task);
                    saveLog(instance.getId(), task.getId(), node.getNodeId(), "error",
                            "节点执行失败但跳过: " + result.getErrorMsg());
                    // 构造一个空的 success result 继续流转
                    NodeExecuteResult skipResult = NodeExecuteResult.success(new JSONObject());
                    transitionEngine.transition(instance, node, edges, nodes, context, skipResult);
                } else {
                    handleFail(instance, node, task, result);
                }
            }

        } catch (Exception e) {
            log.error("[流程实例:{}] 节点执行异常: {}", instance.getId(), node.getNodeName(), e);
            handleException(instance, node, e);
        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    private FlowTask getOrCreateTask(FlowInstance instance, FlowNode node) {
        List<FlowTask> tasks = flowTaskService.list(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<FlowTask>()
                        .eq("instance_id", instance.getId())
                        .eq("node_id", node.getNodeId())
                        .orderByDesc("create_time")
        );
        if (tasks != null && !tasks.isEmpty()) {
            return tasks.get(0);
        }
        FlowTask task = new FlowTask();
        task.setInstanceId(instance.getId());
        task.setNodeId(node.getNodeId());
        task.setNodeName(node.getNodeName());
        task.setNodeType(node.getNodeType());
        task.setStatus(FlowTaskStatusEnum.PENDING.getCode());
        task.setExecuteCount(0);
        task.setCreateTime(LocalDateTime.now());
        flowTaskService.save(task);
        return task;
    }

    private void handleWaiting(FlowTask task, NodeExecuteResult result) {
        task.setStatus(FlowTaskStatusEnum.WAITING.getCode());
        long delaySec = (result.getNextExecuteTime() - System.currentTimeMillis()) / 1000;
        task.setNextExecuteTime(LocalDateTime.now().plusSeconds(Math.max(delaySec, 1)));
        flowTaskService.updateById(task);
    }

    private void handleFail(FlowInstance instance, FlowNode node, FlowTask task, NodeExecuteResult result) {
        log.error("[流程实例:{}] 节点执行失败: {}, error={}",
                instance.getId(), node.getNodeName(), result.getErrorMsg());

        task.setStatus(FlowTaskStatusEnum.FAIL.getCode());
        task.setErrorMsg(result.getErrorMsg());
        task.setEndTime(LocalDateTime.now());
        flowTaskService.updateById(task);

        String failStrategy = node.getFailStrategy();
        if ("skip".equals(failStrategy)) {
            log.info("[流程实例:{}] 失败策略为跳过", instance.getId());
            saveLog(instance.getId(), task.getId(), node.getNodeId(), "error",
                    "节点执行失败但跳过: " + result.getErrorMsg());
        } else {
            instance.setStatus(FlowInstanceStatusEnum.SUSPENDED.getCode());
            instance.setUpdateTime(LocalDateTime.now());
            flowInstanceService.updateById(instance);
            saveLog(instance.getId(), task.getId(), node.getNodeId(), "error",
                    "节点执行失败，流程挂起: " + result.getErrorMsg());
        }
    }

    private void handleException(FlowInstance instance, FlowNode node, Exception e) {
        instance.setStatus(FlowInstanceStatusEnum.FAILED.getCode());
        instance.setUpdateTime(LocalDateTime.now());
        flowInstanceService.updateById(instance);
        saveLog(instance.getId(), null, node != null ? node.getNodeId() : null, "error",
                "节点执行异常: " + e.getMessage());
    }

    /**
     * 同步执行流程实例
     * 在当前线程内串行驱动节点执行，直到结束或异常，不经过 FlowScheduler 调度
     *
     * @param flowId      流程定义ID
     * @param flowCode    流程编码
     * @param version     流程版本号
     * @param businessKey 业务主键
     * @param itemCode    事项编码
     * @param variables   初始上下文变量
     * @param timeoutMs   超时时间（毫秒）
     * @return 最终流程上下文数据
     */
    public Map<String, Object> executeSync(Long flowId, String flowCode, Integer version,
                                           String businessKey, String itemCode,
                                           Map<String, Object> variables, long timeoutMs) {
        long t0 = System.currentTimeMillis();
        long t1, t2, t3, t4, t5;
        
        // 1. 启动实例
        FlowInstance instance = startInstance(flowId, flowCode, version, businessKey, itemCode);
        t1 = System.currentTimeMillis();
        log.info("[同步流程实例:{}] 启动同步执行, flowCode={}, timeoutMs={}, startInstance耗时={}ms",
                instance.getId(), flowCode, timeoutMs, t1 - t0);

        // 2. 注入初始变量（不立即更新数据库，在最终持久化时统一更新）
        FlowContext context = FlowContext.fromJson(instance.getContextJson());
        context.set("_instanceId", instance.getId());  // 确保instanceId在context中
        if (variables != null && !variables.isEmpty()) {
            for (Map.Entry<String, Object> entry : variables.entrySet()) {
                context.set(entry.getKey(), entry.getValue());
            }
        }
        t2 = System.currentTimeMillis();

        // 3. 加载节点和边
        List<FlowNode> nodes = flowNodeService.list(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<FlowNode>()
                        .eq("flow_id", flowId)
                        .eq("del_flag", 0));
        List<FlowEdge> edges = flowEdgeService.list(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<FlowEdge>()
                        .eq("flow_id", flowId)
                        .eq("del_flag", 0));
        t3 = System.currentTimeMillis();
        log.info("[同步流程实例:{}] 加载节点和边耗时={}ms", instance.getId(), t3 - t2);

        if (nodes == null || nodes.isEmpty()) {
            throw new com.riverflow.common.exception.BusinessException("流程定义缺少节点");
        }

        // 4. 找到开始节点
        FlowNode startNode = nodes.stream()
                .filter(n -> FlowNodeTypeEnum.START.getCode().equals(n.getNodeType()))
                .findFirst()
                .orElse(null);
        if (startNode == null) {
            throw new com.riverflow.common.exception.BusinessException("流程定义缺少开始节点");
        }

        saveLog(instance.getId(), null, startNode.getNodeId(), "start",
                "同步流程启动成功, version=" + version);

        // 5. 流转到开始节点后的第一个节点
        FlowNode currentNode = findNextNode(instance, startNode, edges, nodes, context,
                NodeExecuteResult.success(new JSONObject()));
        if (currentNode != null) {
            instance.setCurrentNodeId(currentNode.getNodeId());
        }
        t4 = System.currentTimeMillis();

        // 同步模式日志缓存，最后批量写入
        List<FlowLog> syncLogs = new ArrayList<>();

        // 6. 主执行循环（同步模式：不每次更新数据库，只在结束时保存）
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (currentNode != null
                && !FlowNodeTypeEnum.END.getCode().equals(currentNode.getNodeType())) {

            if (System.currentTimeMillis() > deadline) {
                handleSyncTimeout(instance, currentNode, syncLogs);
                throw new com.riverflow.common.exception.BusinessException(
                        "同步流程执行超时（限制" + timeoutMs + "ms）");
            }

            // 同步模式不支持定时节点
            if (FlowNodeTypeEnum.TIMER.getCode().equals(currentNode.getNodeType())) {
                handleSyncFail(instance, currentNode, "同步流程不支持定时节点", syncLogs);
                throw new com.riverflow.common.exception.BusinessException(
                        "同步流程不支持定时节点: " + currentNode.getNodeName());
            }

            log.info("[同步流程实例:{}] 开始执行节点: {} (type={})",
                    instance.getId(), currentNode.getNodeName(), currentNode.getNodeType());

            // 获取执行器并执行
            NodeExecutor executor = nodeExecutorFactory.getExecutor(currentNode.getNodeType());
            long nodeStart = System.currentTimeMillis();
            NodeExecuteResult result = executor.execute(currentNode, context);
            long nodeCost = System.currentTimeMillis() - nodeStart;
            log.info("[同步流程实例:{}] 节点 {} 执行耗时 {}ms", instance.getId(), currentNode.getNodeName(), nodeCost);

            // 处理执行结果
            if (!result.isSuccess()) {
                if ("skip".equals(currentNode.getFailStrategy())) {
                    log.info("[同步流程实例:{}] 节点 {} 执行失败但策略为跳过，继续流转: {}",
                            instance.getId(), currentNode.getNodeName(), result.getErrorMsg());
                    addSyncLog(syncLogs, instance.getId(), null, currentNode.getNodeId(), "error",
                            "节点执行失败但跳过: " + result.getErrorMsg());
                    result = NodeExecuteResult.success(new JSONObject());
                } else {
                    handleSyncFail(instance, currentNode, result.getErrorMsg(), syncLogs);
                    throw new com.riverflow.common.exception.BusinessException(
                            "节点执行失败 [" + currentNode.getNodeName() + "]: " + result.getErrorMsg());
                }
            }

            if (result.isWaiting()) {
                handleSyncFail(instance, currentNode, "同步流程不支持等待状态", syncLogs);
                throw new com.riverflow.common.exception.BusinessException(
                        "同步流程不支持等待状态: " + currentNode.getNodeName());
            }

            // 保存节点结果到上下文（内存操作，不持久化）
            if (result.getData() != null) {
                context.set("nodeResult_" + currentNode.getNodeId(), result.getData());
            }

            addSyncLog(syncLogs, instance.getId(), null, currentNode.getNodeId(), "execute",
                    "节点执行成功: " + currentNode.getNodeName());

            // 流转到下一节点
            FlowNode nextNode = findNextNode(instance, currentNode, edges, nodes, context, result);
            if (nextNode != null) {
                log.info("[同步流程实例:{}] 从 [{}] 流转到 [{}]", instance.getId(),
                        currentNode.getNodeName(), nextNode.getNodeName());
                instance.setCurrentNodeId(nextNode.getNodeId());
            }
            currentNode = nextNode;
        }

        // 7. 结束处理（只在结束时持久化一次）
        if (currentNode != null && FlowNodeTypeEnum.END.getCode().equals(currentNode.getNodeType())) {
            // 执行结束节点（处理输入映射，将变量提取到上下文顶层）
            NodeExecutor endExecutor = nodeExecutorFactory.getExecutor(currentNode.getNodeType());
            NodeExecuteResult endResult = endExecutor.execute(currentNode, context);
            if (endResult.getData() != null) {
                context.set("nodeResult_" + currentNode.getNodeId(), endResult.getData());
            }

            log.info("[同步流程实例:{}] 流程执行完成", instance.getId());
            instance.setStatus(FlowInstanceStatusEnum.COMPLETED.getCode());
            instance.setEndTime(LocalDateTime.now());
            addSyncLog(syncLogs, instance.getId(), null, currentNode.getNodeId(), "transition", "流程执行完成");
        } else if (currentNode == null) {
            log.error("[同步流程实例:{}] 没有匹配到任何出边，流程挂起", instance.getId());
            instance.setStatus(FlowInstanceStatusEnum.SUSPENDED.getCode());
        }

        // 同步模式：最后统一持久化
        long persistStart = System.currentTimeMillis();
        instance.setContextJson(context.toJsonString());
        instance.setUpdateTime(LocalDateTime.now());
        flowInstanceService.updateById(instance);
        if (!syncLogs.isEmpty()) {
            asyncLogService.saveBatchAsync(syncLogs);
        }
        long totalCost = System.currentTimeMillis() - t0;
        long persistCost = System.currentTimeMillis() - persistStart;
        long nodeExecCost = persistStart - t4;
        log.info("[同步流程实例:{}] 流程执行完成, 总耗时={}ms [启动={}ms, 变量注入={}ms, 加载节点={}ms, 节点执行={}ms, 持久化={}ms]",
                instance.getId(), totalCost, t1-t0, t2-t1, t3-t2, nodeExecCost, persistCost);

        // 8. 构建同步输出结果（只返回 end 节点 inputMapping 绑定的字段，组装为嵌套结构）
        Map<String, Object> output = buildSyncOutput(currentNode, context);
        return output;
    }

    /**
     * 根据 end 节点的 inputMapping 构建同步输出结果，支持嵌套结构如 data.a0188 -> {"data":{"a0188":"..."}}
     */
    private Map<String, Object> buildSyncOutput(FlowNode endNode, FlowContext context) {
        Map<String, Object> result = new HashMap<>();
        if (endNode == null) return result;

        String inputMapping = endNode.getInputMapping();
        if (inputMapping == null || inputMapping.isEmpty()) return result;

        try {
            JSONArray mappings = JSON.parseArray(inputMapping);
            for (int i = 0; i < mappings.size(); i++) {
                JSONObject map = mappings.getJSONObject(i);
                String source = map.getString("source");
                String target = map.getString("target");
                if (source == null || target == null) continue;

                Object value = context.getByPath(source);
                if (value != null) {
                    setNestedOutputValue(result, target, value);
                }
            }
        } catch (Exception e) {
            log.warn("[同步流程] 构建输出结果失败", e);
        }
        return result;
    }

    private void setNestedOutputValue(Map<String, Object> map, String path, Object value) {
        String[] keys = path.split("\\.");
        Map<String, Object> current = map;
        for (int i = 0; i < keys.length - 1; i++) {
            String key = keys[i];
            if (!current.containsKey(key) || !(current.get(key) instanceof Map)) {
                current.put(key, new HashMap<String, Object>());
            }
            current = (Map<String, Object>) current.get(key);
        }
        current.put(keys[keys.length - 1], value);
    }

    /**
     * 同步执行模式下流转到下一节点（不创建 FlowTask，不经过调度器）
     */
    private FlowNode findNextNode(FlowInstance instance, FlowNode currentNode,
                                  List<FlowEdge> edges, List<FlowNode> nodes,
                                  FlowContext context, NodeExecuteResult result) {
        String currentNodeId = currentNode.getNodeId();

        List<FlowEdge> outEdges = edges.stream()
                .filter(e -> e.getSourceNode().equals(currentNodeId))
                .sorted(Comparator.comparingInt(FlowEdge::getPriority))
                .collect(Collectors.toList());

        if (outEdges.isEmpty()) {
            if (FlowNodeTypeEnum.END.getCode().equals(currentNode.getNodeType())) {
                return null;
            }
            log.warn("[同步流程实例:{}] 节点 {} 没有出边", instance.getId(), currentNode.getNodeName());
            return null;
        }

        FlowEdge matchedEdge = null;
        for (FlowEdge edge : outEdges) {
            if (matchEdgeCondition(edge, context, result)) {
                matchedEdge = edge;
                break;
            }
        }

        if (matchedEdge == null) {
            log.error("[同步流程实例:{}] 没有匹配到任何边", instance.getId());
            return null;
        }

        String targetNodeId = matchedEdge.getTargetNode();
        return nodes.stream()
                .filter(n -> n.getNodeId().equals(targetNodeId))
                .findFirst()
                .orElse(null);
    }

    /**
     * 匹配边的条件（复用 TransitionEngine 的核心逻辑）
     */
    private boolean matchEdgeCondition(FlowEdge edge, FlowContext context, NodeExecuteResult result) {
        String conditionType = edge.getConditionType();

        if ("default".equals(conditionType)) {
            return true;
        }
        if ("success".equals(conditionType)) {
            return result.isSuccess() && !result.isWaiting();
        }
        if ("fail".equals(conditionType)) {
            return !result.isSuccess();
        }
        if ("custom".equals(conditionType)) {
            String expression = edge.getConditionExpression();
            if (expression == null || expression.isEmpty()) {
                return true;
            }
            context.set("_lastResult", result.getData());
            Map<String, Object> spelContext = new HashMap<>();
            spelContext.put("context", context.toMap());
            return com.riverflow.common.util.SpelUtil.evaluateBoolean(expression, spelContext);
        }
        return false;
    }

    private void handleSyncTimeout(FlowInstance instance, FlowNode node, List<FlowLog> syncLogs) {
        log.error("[同步流程实例:{}] 节点 {} 执行超时", instance.getId(), node.getNodeName());
        instance.setStatus(FlowInstanceStatusEnum.FAILED.getCode());
        addSyncLog(syncLogs, instance.getId(), null, node.getNodeId(), "error", "同步流程执行超时");
    }

    private void handleSyncFail(FlowInstance instance, FlowNode node, String errorMsg, List<FlowLog> syncLogs) {
        log.error("[同步流程实例:{}] 节点 {} 执行失败: {}",
                instance.getId(), node.getNodeName(), errorMsg);
        instance.setStatus(FlowInstanceStatusEnum.FAILED.getCode());
        addSyncLog(syncLogs, instance.getId(), null, node.getNodeId(), "error",
                "同步流程执行失败: " + errorMsg);
    }

    private void addSyncLog(List<FlowLog> syncLogs, Long instanceId, Long taskId, String nodeId, String logType, String content) {
        FlowLog log = new FlowLog();
        log.setInstanceId(instanceId);
        log.setTaskId(taskId);
        log.setNodeId(nodeId);
        log.setLogType(logType);
        log.setLogContent(content);
        log.setCreateTime(LocalDateTime.now());
        syncLogs.add(log);
    }

    /**
     * 保存流程日志（异步流程使用，带重试）
     */
    private void saveLog(Long instanceId, Long taskId, String nodeId, String logType, String content) {
        try {
            FlowLog flowLog = new FlowLog();
            flowLog.setInstanceId(instanceId);
            flowLog.setTaskId(taskId);
            flowLog.setNodeId(nodeId);
            flowLog.setLogType(logType);
            flowLog.setLogContent(content);
            flowLog.setCreateTime(LocalDateTime.now());
            asyncLogService.saveLogWithRetry(flowLog, 3);
        } catch (Exception e) {
            log.error("保存流程日志失败", e);
        }
    }
}
