package com.riverflow.admin.modules.workflow.scheduler;

import com.riverflow.admin.modules.workflow.engine.FlowEngine;
import com.riverflow.admin.service.FlowEdgeService;
import com.riverflow.admin.service.FlowInstanceService;
import com.riverflow.admin.service.FlowNodeService;
import com.riverflow.admin.service.FlowTaskService;
import com.riverflow.api.entity.FlowEdge;
import com.riverflow.api.entity.FlowInstance;
import com.riverflow.api.entity.FlowNode;
import com.riverflow.api.entity.FlowTask;
import com.riverflow.api.enums.FlowInstanceStatusEnum;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/**
 * 流程调度器（分布式优化版）
 * 使用 ShedLock 确保同一时刻只有一个节点执行扫描
 */
@Slf4j
@Component
public class FlowSchedulerOptimized {

    @Autowired
    private FlowTaskService flowTaskService;
    @Autowired
    private FlowInstanceService flowInstanceService;
    @Autowired
    private FlowNodeService flowNodeService;
    @Autowired
    private FlowEdgeService flowEdgeService;
    @Autowired
    @Qualifier("flowExecutor")
    private ExecutorService flowExecutor;
    @Autowired
    private FlowEngine flowEngine;

    /**
     * 每10秒扫描一次待执行任务
     * 
     * ShedLock 配置说明：
     * - name: 锁的名称，全局唯一
     * - lockAtMostFor: 锁的最长持有时间（必须小于扫描间隔）
     * - lockAtLeastFor: 锁的最短持有时间（防止短时间内重复执行）
     */
    @Scheduled(fixedRate = 10000)
    @SchedulerLock(
        name = "FlowScheduler_scanPendingTasks", 
        lockAtMostFor = "9s", 
        lockAtLeastFor = "1s"
    )
    public void scanPendingTasks() {
        try {
            List<FlowTask> pendingTasks = flowTaskService.getPendingTasks(LocalDateTime.now());
            if (pendingTasks == null || pendingTasks.isEmpty()) {
                return;
            }

            log.debug("扫描到 {} 个待执行任务", pendingTasks.size());

            Set<Long> submittedInstances = ConcurrentHashMap.newKeySet();

            for (FlowTask task : pendingTasks) {
                if (!submittedInstances.add(task.getInstanceId())) {
                    continue;
                }

                flowExecutor.submit(() -> {
                    try {
                        FlowInstance instance = flowInstanceService.getById(task.getInstanceId());
                        if (instance == null) {
                            log.warn("任务对应的实例不存在: taskId={}", task.getId());
                            return;
                        }
                        if (!FlowInstanceStatusEnum.RUNNING.getCode().equals(instance.getStatus())) {
                            log.debug("实例状态不是运行中，跳过: instanceId={}, status={}",
                                    instance.getId(), instance.getStatus());
                            return;
                        }

                        List<FlowNode> nodes = flowNodeService.getNodesByFlowId(instance.getFlowId());
                        List<FlowEdge> edges = flowEdgeService.getEdgesByFlowId(instance.getFlowId());

                        FlowNode currentNode = nodes.stream()
                                .filter(n -> n.getNodeId().equals(task.getNodeId()))
                                .findFirst().orElse(null);

                        if (currentNode == null) {
                            log.warn("任务节点 {} 在流程定义中不存在，跳过执行: taskId={}",
                                    task.getNodeId(), task.getId());
                            return;
                        }

                        if (!task.getNodeId().equals(instance.getCurrentNodeId())) {
                            log.warn("任务节点 {} 与实例当前节点 {} 不一致，跳过执行: instanceId={}",
                                    task.getNodeId(), instance.getCurrentNodeId(), instance.getId());
                            return;
                        }

                        flowEngine.executeNode(instance, currentNode, edges, nodes);

                    } catch (Exception e) {
                        log.error("异步调度执行任务失败: taskId={}", task.getId(), e);
                    }
                });
            }
        } catch (Exception e) {
            log.error("扫描待执行任务异常", e);
        }
    }
}
