package com.riverflow.admin.modules.workflow.engine;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.riverflow.admin.modules.workflow.context.FlowContext;
import com.riverflow.admin.modules.workflow.loop.LoopAsyncCoordinator;
import com.riverflow.admin.modules.workflow.loop.LoopState;
import com.riverflow.admin.modules.workflow.loop.LoopTaskHelper;
import com.riverflow.admin.modules.workflow.loop.LoopUtils;
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
import com.riverflow.api.enums.FlowTaskTypeEnum;
import com.riverflow.common.constant.CommonConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 流程执行引擎
 * <p>
 * 完整阶段扩展：
 * 1. 异步模式支持循环任务合并（循环体内部节点连续执行，不创建大量 pending 任务）
 * 2. 分布式锁支持同线程可重入（递归执行循环链时不会死锁）
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
    @Autowired
    private LoopAsyncCoordinator loopAsyncCoordinator;

    /**
     * 当前线程已持有的流程实例锁（用于循环链递归执行时避免死锁）
     */
    private final ThreadLocal<Set<Long>> holdingInstanceLocks = ThreadLocal.withInitial(HashSet::new);

    /**
     * 当前执行链优先使用的任务（用于循环任务合并时复用同一条任务记录）
     */
    private final ThreadLocal<FlowTask> currentTaskHolder = new ThreadLocal<>();

    /**
     * 单次执行链中最多连续执行的节点数，防止栈溢出/长事务
     */
    private static final int MAX_CHAIN_LENGTH = 1000;

    /**
     * 同步流程是否持久化流程实例（insert/update wf_flow_instance）
     */
    @Value("${riverflow.sync.persist-instance:true}")
    private boolean syncPersistInstance;

    /**
     * 同步流程是否保存日志（wf_flow_log）
     */
    @Value("${riverflow.sync.save-log:true}")
    private boolean syncSaveLog;

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
     * 启动同步流程实例
     * 根据 syncPersistInstance 配置决定是否持久化到数据库
     */
    public FlowInstance startSyncInstance(Long flowId, String flowCode, Integer version,
                                          String businessKey, String itemCode) {
        log.info("启动同步流程实例: flowCode={}, version={}, businessKey={}, persist={}",
                flowCode, version, businessKey, syncPersistInstance);

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

        Long instanceId;
        if (syncPersistInstance) {
            flowInstanceService.save(instance);
            instanceId = instance.getId();
        } else {
            instanceId = IdWorker.getId();
            instance.setId(instanceId);
            log.debug("同步流程实例跳过持久化, 使用临时instanceId={}", instanceId);
        }
        context.set("_instanceId", instanceId);

        saveSyncLog(instanceId, null, null, "start", "流程实例启动成功, version=" + version);
        return instance;
    }

    /**
     * 执行流程实例的当前节点（异步/调度模式入口）
     * <p>
     * 完整阶段：先定位当前节点待执行的 pending/waiting 任务，再交给 executeTask 执行。
     * 这样可以避免按 node_id 取最新任务导致的循环入口节点误判。
     */
    @Transactional(rollbackFor = Exception.class)
    public void executeNode(FlowInstance instance, FlowNode node, List<FlowEdge> edges, List<FlowNode> nodes) {
        // 同步实例状态
        FlowInstance freshInstance = flowInstanceService.getById(instance.getId());
        if (freshInstance == null || !FlowInstanceStatusEnum.RUNNING.getCode().equals(freshInstance.getStatus())) {
            log.warn("[流程实例:{}] 实例状态不是运行中，跳过执行", instance.getId());
            return;
        }
        instance.setStatus(freshInstance.getStatus());
        instance.setCurrentNodeId(freshInstance.getCurrentNodeId());
        instance.setContextJson(freshInstance.getContextJson());

        // 找到当前节点待执行的任务；没有则创建
        FlowTask task = flowTaskService.getOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<FlowTask>()
                        .eq("instance_id", instance.getId())
                        .eq("node_id", node.getNodeId())
                        .in("status", FlowTaskStatusEnum.PENDING.getCode(), FlowTaskStatusEnum.WAITING.getCode())
                        .orderByDesc("create_time")
                        .last("LIMIT 1")
        );
        if (task == null) {
            task = createPendingTask(instance, node, null);
        }

        // 循环子任务/汇聚任务走专门入口
        String taskType = task.getTaskType();
        if (FlowTaskTypeEnum.LOOP_ITERATION.getCode().equals(taskType)) {
            executeLoopIterationTask(instance, task, nodes, edges);
            return;
        }
        if (FlowTaskTypeEnum.LOOP_AGGREGATE.getCode().equals(taskType)) {
            executeLoopAggregateTask(instance, task, nodes, edges);
            return;
        }
        executeTask(instance, task, node, edges, nodes);
    }

    /**
     * 执行指定任务（调度器/协调器入口）
     */
    @Transactional(rollbackFor = Exception.class)
    public void executeTask(FlowInstance instance, FlowTask task, FlowNode node, List<FlowEdge> edges, List<FlowNode> nodes) {
        if (task == null) {
            log.warn("[流程实例:{}] 待执行任务为空，跳过", instance.getId());
            return;
        }
        currentTaskHolder.set(task);
        String lockKey = CommonConstant.FLOW_LOCK_PREFIX + instance.getId();
        Set<Long> heldLocks = holdingInstanceLocks.get();
        boolean lockAcquired = false;

        // 同线程锁重入：已在执行链中则不再重复获取
        if (!heldLocks.contains(instance.getId())) {
            int nodeTimeout = node.getTimeout() != null ? node.getTimeout() : 30000;
            int lockSeconds = Math.max(30, nodeTimeout / 1000 + 10);
            Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", lockSeconds, TimeUnit.SECONDS);
            if (!Boolean.TRUE.equals(locked)) {
                log.warn("[流程实例:{}] 获取分布式锁失败，跳过本次执行", instance.getId());
                return;
            }
            heldLocks.add(instance.getId());
            lockAcquired = true;
        }

        try {
            // 入口校验：实例状态
            FlowInstance freshInstance = flowInstanceService.getById(instance.getId());
            if (freshInstance == null || !FlowInstanceStatusEnum.RUNNING.getCode().equals(freshInstance.getStatus())) {
                log.warn("[流程实例:{}] 实例状态不是运行中，跳过执行", instance.getId());
                return;
            }
            instance.setStatus(freshInstance.getStatus());
            instance.setCurrentNodeId(freshInstance.getCurrentNodeId());
            instance.setContextJson(freshInstance.getContextJson());

            // 幂等校验：按具体任务状态
            if (!FlowTaskStatusEnum.PENDING.getCode().equals(task.getStatus())
                    && !FlowTaskStatusEnum.WAITING.getCode().equals(task.getStatus())) {
                log.warn("[流程实例:{}] 任务已被其他线程执行（taskId={}, status={}），跳过",
                        instance.getId(), task.getId(), task.getStatus());
                return;
            }

            doExecuteNode(instance, node, edges, nodes);
        } catch (Exception e) {
            log.error("[流程实例:{}] 节点执行异常: {}", instance.getId(), node.getNodeName(), e);
            handleException(instance, node, task, e);
        } finally {
            currentTaskHolder.remove();
            if (lockAcquired) {
                redisTemplate.delete(lockKey);
                heldLocks.remove(instance.getId());
            }
        }
    }

    /**
     * 实际节点执行逻辑，支持循环链连续执行
     */
    private void doExecuteNode(FlowInstance instance, FlowNode startNode, List<FlowEdge> edges, List<FlowNode> nodes) {
        FlowNode currentNode = startNode;
        int chainGuard = 0;
        FlowContext context = null;

        while (currentNode != null && chainGuard++ < MAX_CHAIN_LENGTH) {
            // 每次循环刷新实例状态
            FlowInstance freshInstance = flowInstanceService.getById(instance.getId());
            if (freshInstance == null || !FlowInstanceStatusEnum.RUNNING.getCode().equals(freshInstance.getStatus())) {
                log.warn("[流程实例:{}] 实例状态不是运行中，终止执行链", instance.getId());
                return;
            }
            instance.setStatus(freshInstance.getStatus());
            instance.setCurrentNodeId(freshInstance.getCurrentNodeId());
            instance.setContextJson(freshInstance.getContextJson());

            // 首次进入或已退出作用域时重新构建上下文；
            // 循环体内（包括循环回跳）复用当前 context，以保留 scopeStack 中的 item/index 等循环变量。
            if (context == null || !context.inScope()) {
                context = buildContext(instance, nodes, edges);
            }
            NodeExecuteResult result = executeSingleNode(instance, currentNode, edges, nodes, context);
            log.info("[流程实例:{}] executeSingleNode 返回: success={}, waiting={}, terminateChain={}, nextEntryNodeId={}, exitLoop={}, inScope={}",
                    instance.getId(), result != null && result.isSuccess(),
                    result != null && result.isWaiting(),
                    result != null && result.isTerminateChain(),
                    result != null ? result.getNextEntryNodeId() : null,
                    result != null && result.isExitLoop(),
                    context != null && context.inScope());

            if (result == null) {
                // 等待/失败/异常已处理，终止执行链
                return;
            }

            // 显式终止当前执行链（如异步并行 foreach 调度完成后）
            if (result.isTerminateChain()) {
                log.info("[流程实例:{}] 节点 {} 要求终止当前执行链",
                        instance.getId(), currentNode.getNodeName());
                return;
            }

            // 循环回跳：直接在当前链中继续执行入口节点
            if (result.getNextEntryNodeId() != null && !result.getNextEntryNodeId().isEmpty()) {
                log.info("[流程实例:{}] doExecuteNode 检测到循环回跳: currentNode={}, entryNodeId={}",
                        instance.getId(), currentNode.getNodeId(), result.getNextEntryNodeId());
                FlowNode entryNode = findNode(nodes, result.getNextEntryNodeId());
                if (entryNode == null) {
                    log.error("[流程实例:{}] 循环跳转目标节点不存在: {}",
                            instance.getId(), result.getNextEntryNodeId());
                    instance.setStatus(FlowInstanceStatusEnum.SUSPENDED.getCode());
                    flowInstanceService.updateById(instance);
                    return;
                }
                // timer 节点不能连续执行，必须走调度器
                if (FlowNodeTypeEnum.TIMER.getCode().equals(entryNode.getNodeType())) {
                    transitionEngine.transition(instance, currentNode, edges, nodes, context, result);
                    return;
                }
                log.info("[流程实例:{}] 循环任务合并：从 [{}] 回跳到 [{}] 继续执行",
                        instance.getId(), currentNode.getNodeName(), entryNode.getNodeName());
                currentNode = entryNode;
                continue;
            }

            // 已退出循环作用域，走正常 TransitionEngine 流转
            if (!context.inScope()) {
                log.info("[流程实例:{}] doExecuteNode 已退出循环作用域，走 TransitionEngine: currentNode={}",
                        instance.getId(), currentNode.getNodeId());
                transitionEngine.transition(instance, currentNode, edges, nodes, context, result);
                return;
            }

            // 仍在循环体内：找到下一个节点继续执行
            log.info("[流程实例:{}] doExecuteNode 仍在循环体内，查找下一节点: currentNode={}",
                    instance.getId(), currentNode.getNodeId());
            FlowNode nextNode = findNextNode(instance, currentNode, edges, nodes, context, result);
            log.info("[流程实例:{}] doExecuteNode 找到下一节点: currentNode={}, nextNode={}",
                    instance.getId(), currentNode.getNodeId(), nextNode != null ? nextNode.getNodeId() : null);
            if (nextNode == null || FlowNodeTypeEnum.END.getCode().equals(nextNode.getNodeType())) {
                transitionEngine.transition(instance, currentNode, edges, nodes, context, result);
                return;
            }
            currentNode = nextNode;
        }

        // 超过最大链长，创建 pending 任务由调度器接力
        if (chainGuard >= MAX_CHAIN_LENGTH && currentNode != null) {
            log.warn("[流程实例:{}] 执行链超过最大长度 {}，创建 pending 任务接力",
                    instance.getId(), MAX_CHAIN_LENGTH);
            createPendingTask(instance, currentNode, context);
        }
    }

    /**
     * 执行单个节点，返回执行结果；若进入等待/失败/异常则返回 null
     */
    private NodeExecuteResult executeSingleNode(FlowInstance instance, FlowNode node,
                                                List<FlowEdge> edges, List<FlowNode> nodes,
                                                FlowContext context) {
        log.info("[流程实例:{}] 开始执行节点: {} (type={})",
                instance.getId(), node.getNodeName(), node.getNodeType());

        // 优先使用调度器/协调器指定的任务；若与当前节点不一致则回退到按 node_id 取最新任务
        FlowTask preferredTask = currentTaskHolder.get();
        FlowTask task;
        if (preferredTask != null && node.getNodeId().equals(preferredTask.getNodeId())) {
            task = preferredTask;
        } else {
            task = getOrCreateTask(instance, node);
        }

        // 旧实例兼容：timer 节点已被 FlowScheduler 唤醒（WAITING 且 next_execute_time 已到达）
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
            return null;
        }

        task.setStatus(FlowTaskStatusEnum.RUNNING.getCode());
        task.setStartTime(LocalDateTime.now());
        task.setInputContext(context.toJsonString());
        task.setExecuteCount(task.getExecuteCount() + 1);
        // 循环任务标记
        if (task.getTaskType() == null) {
            task.setTaskType(FlowTaskTypeEnum.NODE.getCode());
        }
        LoopTaskHelper.fillLoopFields(task, context);
        flowTaskService.updateById(task);

        NodeExecutor executor = nodeExecutorFactory.getExecutor(node.getNodeType());
        NodeExecuteResult result = executor.execute(node, context);

        if (result.isSuccess()) {
            if (result.isWaiting()) {
                handleWaiting(task, result);
                return null;
            }
            if (result.getData() != null) {
                context.set("nodeResult_" + node.getNodeId(), result.getData());
            }
            task.setOutputContext(context.toJsonString());
            task.setResultJson(JSON.toJSONString(result.getData()));
            task.setStatus(FlowTaskStatusEnum.SUCCESS.getCode());
            task.setEndTime(LocalDateTime.now());
            flowTaskService.updateById(task);

            instance.setContextJson(context.toJsonString());
            instance.setCurrentNodeId(node.getNodeId());
            flowInstanceService.updateById(instance);

            saveLog(instance.getId(), task.getId(), node.getNodeId(), "execute",
                    "节点执行成功: " + node.getNodeName());
            return result;
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
                return NodeExecuteResult.success(new JSONObject());
            } else {
                handleFail(instance, node, task, result);
                return null;
            }
        }
    }

    private FlowContext buildContext(FlowInstance instance, List<FlowNode> nodes, List<FlowEdge> edges) {
        FlowContext context = FlowContext.fromJson(instance.getContextJson());
        if (context.getInstanceId() == null) {
            context.set("_instanceId", instance.getId());
            context.set("_businessKey", instance.getBusinessKey());
            context.set("_flowCode", instance.getFlowCode());
        }
        context.setNodes(nodes);
        context.setEdges(edges);
        context.setAsyncMode(currentTaskHolder.get() != null);
        // 从持久化的 LoopState 恢复循环作用域，确保异步调度或直接执行循环体节点时 item/index 可用
        restoreLoopScope(context);
        return context;
    }

    /**
     * 根据上下文中的 _loop_state_* 恢复 scopeStack。
     * 当任务由调度器单独拉起时，scopeStack 不会从数据库恢复，必须在这里重建。
     */
    private void restoreLoopScope(FlowContext context) {
        if (context == null) {
            return;
        }
        Map<String, Object> globals = context.getGlobals();
        if (globals == null || globals.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : new HashMap<>(globals).entrySet()) {
            if (!entry.getKey().startsWith("_loop_state_")) {
                continue;
            }
            try {
                LoopState state = LoopState.from(entry.getValue());
                if (state != null && state.isForeach() && state.isInitialized()) {
                    context.pushScope();
                    Object item = state.getCurrentItem(null);
                    if (item != null) {
                        context.set(state.getItemVar(), item);
                    }
                    context.set(state.getIndexVar(), state.getIndex());
                    context.pushLoopFrame(state.getLoopNodeId(), state.getIndex());
                    log.debug("[流程实例:{}] 从 LoopState 恢复循环作用域: loopNodeId={}, itemVar={}, index={}",
                            context.getInstanceId(), state.getLoopNodeId(), state.getItemVar(), state.getIndex());
                }
            } catch (Exception e) {
                log.warn("[流程实例:{}] 恢复 LoopState 作用域失败: key={}",
                        context.getInstanceId(), entry.getKey(), e);
            }
        }
    }

    private FlowNode findNode(List<FlowNode> nodes, String nodeId) {
        if (nodes == null || nodeId == null) {
            return null;
        }
        return nodes.stream()
                .filter(n -> nodeId.equals(n.getNodeId()))
                .findFirst()
                .orElse(null);
    }

    private FlowTask createPendingTask(FlowInstance instance, FlowNode node, FlowContext context) {
        FlowTask task = new FlowTask();
        task.setInstanceId(instance.getId());
        task.setNodeId(node.getNodeId());
        task.setNodeName(node.getNodeName());
        task.setNodeType(node.getNodeType());
        task.setStatus(FlowTaskStatusEnum.PENDING.getCode());
        task.setTaskType(FlowTaskTypeEnum.NODE.getCode());
        task.setExecuteCount(0);
        task.setCreateTime(LocalDateTime.now());
        LoopTaskHelper.fillLoopFields(task, context);
        flowTaskService.save(task);

        instance.setCurrentNodeId(node.getNodeId());
        instance.setUpdateTime(LocalDateTime.now());
        flowInstanceService.updateById(instance);
        return task;
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
        return createPendingTask(instance, node, null);
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

    private void handleException(FlowInstance instance, FlowNode node, FlowTask task, Exception e) {
        instance.setStatus(FlowInstanceStatusEnum.FAILED.getCode());
        instance.setUpdateTime(LocalDateTime.now());
        flowInstanceService.updateById(instance);

        if (task != null) {
            task.setStatus(FlowTaskStatusEnum.FAIL.getCode());
            task.setErrorMsg(e.getMessage());
            task.setEndTime(LocalDateTime.now());
            flowTaskService.updateById(task);
        }

        saveLog(instance.getId(), task != null ? task.getId() : null, node != null ? node.getNodeId() : null, "error",
                "节点执行异常: " + e.getMessage());
    }

    /**
     * 并行循环聚合完成后，继续向后流转
     */
    @Transactional(rollbackFor = Exception.class)
    public void transitionAfterLoop(FlowInstance instance, FlowNode currentNode,
                                    List<FlowEdge> edges, List<FlowNode> nodes,
                                    FlowContext context) {
        transitionEngine.transition(instance, currentNode, edges, nodes, context,
                NodeExecuteResult.success().withExitLoop(true));
    }

    /**
     * 执行 LOOP_ITERATION 子任务（不再加实例级分布式锁）
     * <p>
     * 优化后 iteration 只把结果写 Redis，不写回主 context，多个 iteration 可以真正并行执行。
     * 汇聚阶段（LOOP_AGGREGATE）仍然加实例级锁。
     */
    @Transactional(rollbackFor = Exception.class)
    public void executeLoopIterationTask(FlowInstance instance, FlowTask task,
                                         List<FlowNode> nodes, List<FlowEdge> edges) {
        try {
            loopAsyncCoordinator.executeIterationTask(instance, task, nodes, edges);
        } catch (Exception e) {
            log.error("[流程实例:{}] LOOP_ITERATION 执行异常: taskId={}",
                    instance.getId(), task.getId(), e);
        }
    }

    /**
     * 执行 LOOP_AGGREGATE 汇聚任务（带实例级分布式锁）
     */
    @Transactional(rollbackFor = Exception.class)
    public void executeLoopAggregateTask(FlowInstance instance, FlowTask task,
                                         List<FlowNode> nodes, List<FlowEdge> edges) {
        String lockKey = CommonConstant.FLOW_LOCK_PREFIX + instance.getId();
        Set<Long> heldLocks = holdingInstanceLocks.get();
        boolean lockAcquired = false;
        if (!heldLocks.contains(instance.getId())) {
            Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 30, TimeUnit.SECONDS);
            if (!Boolean.TRUE.equals(locked)) {
                log.warn("[流程实例:{}] LOOP_AGGREGATE 获取分布式锁失败，跳过: taskId={}",
                        instance.getId(), task.getId());
                return;
            }
            heldLocks.add(instance.getId());
            lockAcquired = true;
        }
        try {
            loopAsyncCoordinator.executeAggregateTask(instance, task, nodes, edges);
        } catch (Exception e) {
            log.error("[流程实例:{}] LOOP_AGGREGATE 执行异常: taskId={}",
                    instance.getId(), task.getId(), e);
        } finally {
            if (lockAcquired) {
                redisTemplate.delete(lockKey);
                heldLocks.remove(instance.getId());
            }
        }
    }

    /**
     * 同步执行流程实例
     */
    public Map<String, Object> executeSync(Long flowId, String flowCode, Integer version,
                                           String businessKey, String itemCode,
                                           Map<String, Object> variables, long timeoutMs) {
        long t0 = System.currentTimeMillis();
        long t1, t2, t3, t4;

        FlowInstance instance = startSyncInstance(flowId, flowCode, version, businessKey, itemCode);
        t1 = System.currentTimeMillis();
        log.info("[同步流程实例:{}] 启动同步执行, flowCode={}, timeoutMs={}, startInstance耗时={}ms",
                instance.getId(), flowCode, timeoutMs, t1 - t0);

        FlowContext context = FlowContext.fromJson(instance.getContextJson());
        context.set("_instanceId", instance.getId());
        if (variables != null && !variables.isEmpty()) {
            for (Map.Entry<String, Object> entry : variables.entrySet()) {
                context.set(entry.getKey(), entry.getValue());
            }
        }
        t2 = System.currentTimeMillis();

        List<FlowNode> nodes = flowNodeService.list(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<FlowNode>()
                        .eq("flow_id", flowId)
                        .eq("del_flag", 0));
        List<FlowEdge> edges = flowEdgeService.list(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<FlowEdge>()
                        .eq("flow_id", flowId)
                        .eq("del_flag", 0));

        context.setNodes(nodes);
        context.setEdges(edges);

        t3 = System.currentTimeMillis();
        log.info("[同步流程实例:{}] 加载节点和边耗时={}ms", instance.getId(), t3 - t2);

        if (nodes == null || nodes.isEmpty()) {
            throw new com.riverflow.common.exception.BusinessException("流程定义缺少节点");
        }

        FlowNode startNode = nodes.stream()
                .filter(n -> FlowNodeTypeEnum.START.getCode().equals(n.getNodeType()))
                .findFirst()
                .orElse(null);
        if (startNode == null) {
            throw new com.riverflow.common.exception.BusinessException("流程定义缺少开始节点");
        }

        saveSyncLog(instance.getId(), null, startNode.getNodeId(), "start",
                "同步流程启动成功, version=" + version);

        FlowNode currentNode = findNextNode(instance, startNode, edges, nodes, context,
                NodeExecuteResult.success(new JSONObject()));
        if (currentNode != null) {
            instance.setCurrentNodeId(currentNode.getNodeId());
        }
        t4 = System.currentTimeMillis();

        List<FlowLog> syncLogs = new ArrayList<>();

        long deadline = System.currentTimeMillis() + timeoutMs;
        while (currentNode != null
                && !FlowNodeTypeEnum.END.getCode().equals(currentNode.getNodeType())) {

            if (System.currentTimeMillis() > deadline) {
                handleSyncTimeout(instance, currentNode, syncLogs);
                throw new com.riverflow.common.exception.BusinessException(
                        "同步流程执行超时（限制" + timeoutMs + "ms）");
            }

            if (FlowNodeTypeEnum.TIMER.getCode().equals(currentNode.getNodeType())) {
                handleSyncFail(instance, currentNode, "同步流程不支持定时节点", syncLogs);
                throw new com.riverflow.common.exception.BusinessException(
                        "同步流程不支持定时节点: " + currentNode.getNodeName());
            }

            log.info("[同步流程实例:{}] 开始执行节点: {} (type={})",
                    instance.getId(), currentNode.getNodeName(), currentNode.getNodeType());

            NodeExecutor executor = nodeExecutorFactory.getExecutor(currentNode.getNodeType());
            long nodeStart = System.currentTimeMillis();
            NodeExecuteResult result = executor.execute(currentNode, context);
            long nodeCost = System.currentTimeMillis() - nodeStart;
            log.info("[同步流程实例:{}] 节点 {} 执行耗时 {}ms", instance.getId(), currentNode.getNodeName(), nodeCost);

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

            if (result.getData() != null) {
                context.set("nodeResult_" + currentNode.getNodeId(), result.getData());
            }

            addSyncLog(syncLogs, instance.getId(), null, currentNode.getNodeId(), "execute",
                    "节点执行成功: " + currentNode.getNodeName());

            final String nextEntryNodeId = result.getNextEntryNodeId();
            if (nextEntryNodeId != null && !nextEntryNodeId.isEmpty()) {
                FlowNode entryNode = nodes.stream()
                        .filter(n -> n.getNodeId().equals(nextEntryNodeId))
                        .findFirst().orElse(null);
                if (entryNode == null) {
                    handleSyncFail(instance, currentNode, "循环跳转目标节点不存在: " + result.getNextEntryNodeId(), syncLogs);
                    throw new com.riverflow.common.exception.BusinessException(
                            "循环跳转目标节点不存在: " + result.getNextEntryNodeId());
                }
                log.info("[同步流程实例:{}] 循环回跳: 从 [{}] 到 [{}]",
                        instance.getId(), currentNode.getNodeName(), entryNode.getNodeName());
                instance.setCurrentNodeId(entryNode.getNodeId());
                currentNode = entryNode;
                continue;
            }

            FlowNode nextNode = findNextNode(instance, currentNode, edges, nodes, context, result);
            if (nextNode != null) {
                log.info("[同步流程实例:{}] 从 [{}] 流转到 [{}]", instance.getId(),
                        currentNode.getNodeName(), nextNode.getNodeName());
                instance.setCurrentNodeId(nextNode.getNodeId());
            }
            currentNode = nextNode;
        }

        if (currentNode != null && FlowNodeTypeEnum.END.getCode().equals(currentNode.getNodeType())) {
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

        long persistStart = System.currentTimeMillis();
        instance.setContextJson(context.toJsonString());
        instance.setUpdateTime(LocalDateTime.now());
        if (syncPersistInstance) {
            flowInstanceService.updateById(instance);
        }
        if (!syncLogs.isEmpty()) {
            asyncLogService.saveBatchAsync(syncLogs);
        }
        long totalCost = System.currentTimeMillis() - t0;
        long persistCost = System.currentTimeMillis() - persistStart;
        long nodeExecCost = persistStart - t4;
        log.info("[同步流程实例:{}] 流程执行完成, 总耗时={}ms [启动={}ms, 变量注入={}ms, 加载节点={}ms, 节点执行={}ms, 持久化={}ms], persist={}",
                instance.getId(), totalCost, t1 - t0, t2 - t1, t3 - t2, nodeExecCost, persistCost, syncPersistInstance);

        return buildSyncOutput(currentNode, context);
    }

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
        if (!syncSaveLog) {
            return;
        }
        FlowLog log = new FlowLog();
        log.setInstanceId(instanceId);
        log.setTaskId(taskId);
        log.setNodeId(nodeId);
        log.setLogType(logType);
        log.setLogContent(content);
        log.setCreateTime(LocalDateTime.now());
        syncLogs.add(log);
    }

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

    /**
     * 同步流程专用日志保存
     */
    private void saveSyncLog(Long instanceId, Long taskId, String nodeId, String logType, String content) {
        if (!syncSaveLog) {
            return;
        }
        saveLog(instanceId, taskId, nodeId, logType, content);
    }
}
