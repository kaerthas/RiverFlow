package com.riverflow.admin.modules.workflow.engine;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.riverflow.admin.modules.workflow.context.FlowContext;
import com.riverflow.admin.modules.workflow.node.NodeExecutor;
import com.riverflow.admin.modules.workflow.node.NodeExecutorFactory;
import com.riverflow.admin.service.FlowInstanceService;
import com.riverflow.admin.service.FlowLogService;
import com.riverflow.admin.service.FlowTaskService;
import com.riverflow.api.entity.FlowEdge;
import com.riverflow.api.entity.FlowInstance;
import com.riverflow.api.entity.FlowLog;
import com.riverflow.api.entity.FlowNode;
import com.riverflow.api.entity.FlowTask;
import com.riverflow.api.enums.FlowInstanceStatusEnum;
import com.riverflow.api.enums.FlowTaskStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 流程执行引擎（Redisson 优化版）
 * 使用 Redisson 的分布式锁，支持自动续期（看门狗机制）
 */
@Slf4j
@Component
public class FlowEngineWithRedisson {

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
    private RedissonClient redissonClient;

    /**
     * 启动流程实例
     */
    public FlowInstance startInstance(Long flowId, String flowCode, Integer version,
                                      String businessKey, String itemCode) {
        FlowInstance instance = new FlowInstance();
        instance.setFlowId(flowId);
        instance.setFlowCode(flowCode);
        instance.setVersion(version);
        instance.setBusinessKey(businessKey);
        instance.setStatus(FlowInstanceStatusEnum.RUNNING.getCode());
        instance.setStartTime(LocalDateTime.now());
        instance.setCreateTime(LocalDateTime.now());
        instance.setUpdateTime(LocalDateTime.now());
        instance.setDelFlag(0);
        flowInstanceService.save(instance);

        FlowContext context = new FlowContext(instance.getId(), businessKey, flowCode);
        if (itemCode != null) {
            context.set("itemCode", itemCode);
        }
        instance.setContextJson(context.toJsonString());
        instance.setCurrentNodeId("");
        flowInstanceService.updateById(instance);

        saveLog(instance.getId(), null, null, "start", "流程实例启动成功, version=" + version);
        return instance;
    }

    /**
     * 执行流程实例的当前节点
     * 
     * Redisson 锁的优势：
     * 1. 自动续期（看门狗机制）：默认每10秒续期一次，防止锁过期
     * 2. 可重入锁：同一线程可多次获取锁
     * 3. 更可靠的锁释放：只有持有锁的线程才能释放锁
     */
    @Transactional(rollbackFor = Exception.class)
    public void executeNode(FlowInstance instance, FlowNode node, List<FlowEdge> edges, List<FlowNode> nodes) {
        String lockKey = "flow:lock:" + instance.getId();
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 尝试获取锁
            // waitTime: 等待获取锁的最长时间（5秒）
            // leaseTime: 锁的过期时间（-1 表示启用看门狗自动续期）
            boolean locked = lock.tryLock(5, -1, TimeUnit.SECONDS);
            if (!locked) {
                log.warn("[流程实例:{}] 获取分布式锁失败，跳过本次执行", instance.getId());
                return;
            }

            log.debug("[流程实例:{}] 成功获取分布式锁", instance.getId());

            try {
                // 二次校验
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

                // Timer 节点旧实例兼容
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
                        NodeExecuteResult skipResult = NodeExecuteResult.success(new JSONObject());
                        transitionEngine.transition(instance, node, edges, nodes, context, skipResult);
                    } else {
                        handleFail(instance, node, task, result);
                    }
                }

            } catch (Exception e) {
                log.error("[流程实例:{}] 节点执行异常: {}", instance.getId(), node.getNodeName(), e);
                handleException(instance, node, e);
            }

        } catch (InterruptedException e) {
            log.error("[流程实例:{}] 获取锁被中断", instance.getId(), e);
            Thread.currentThread().interrupt();
        } finally {
            // 只有当前线程持有锁时才释放
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("[流程实例:{}] 释放分布式锁", instance.getId());
            }
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
        instance.setStatus(FlowInstanceStatusEnum.SUSPENDED.getCode());
        instance.setUpdateTime(LocalDateTime.now());
        flowInstanceService.updateById(instance);
        saveLog(instance.getId(), null, node.getNodeId(), "error",
                "节点执行异常，流程挂起: " + e.getMessage());
    }

    private void saveLog(Long instanceId, Long taskId, String nodeId, String logType, String content) {
        try {
            FlowLog log = new FlowLog();
            log.setInstanceId(instanceId);
            log.setTaskId(taskId);
            log.setNodeId(nodeId);
            log.setLogType(logType);
            log.setLogContent(content);
            log.setCreateTime(LocalDateTime.now());
            flowLogService.save(log);
        } catch (Exception e) {
            log.error("保存流转日志失败", e);
        }
    }
}
