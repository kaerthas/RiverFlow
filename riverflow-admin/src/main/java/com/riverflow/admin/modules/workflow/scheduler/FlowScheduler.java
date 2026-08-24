package com.riverflow.admin.modules.workflow.scheduler;

import com.riverflow.admin.modules.workflow.engine.FlowEngine;
import com.riverflow.admin.service.FlowDefinitionService;
import com.riverflow.admin.service.FlowEdgeService;
import com.riverflow.admin.service.FlowInstanceService;
import com.riverflow.admin.service.FlowNodeService;
import com.riverflow.admin.service.FlowTaskService;
import com.riverflow.api.entity.FlowEdge;
import com.riverflow.api.entity.FlowInstance;
import com.riverflow.api.entity.FlowNode;
import com.riverflow.api.entity.FlowTask;
import com.riverflow.api.enums.FlowInstanceStatusEnum;
import com.riverflow.api.enums.FlowTaskTypeEnum;
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
 * 流程调度器
 * 定时扫描待执行的流程任务并驱动执行
 */
@Slf4j
@Component
public class FlowScheduler {

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
     * 每10秒扫描一次待执行任务，提交到线程池异步执行。
     * ShedLock 保证多节点部署时同一时刻只有一个节点执行扫描，
     * 避免每个节点重复扫描数据库造成压力。
     */
    @Scheduled(fixedRate = 10000)
    @SchedulerLock(name = "FlowScheduler_scanPendingTasks", lockAtMostFor = "PT9S", lockAtLeastFor = "PT1S")
    public void scanPendingTasks() {
        try {
            List<FlowTask> pendingTasks = flowTaskService.getPendingTasks(LocalDateTime.now());
            if (pendingTasks == null || pendingTasks.isEmpty()) {
                return;
            }

            log.debug("扫描到 {} 个待执行任务", pendingTasks.size());

            // 同一轮扫描中，同一个实例只提交一次（避免重复提交到线程池）
            Set<Long> submittedInstances = ConcurrentHashMap.newKeySet();

            for (FlowTask task : pendingTasks) {
                // LOOP_ITERATION 任务允许同实例多任务并发调度（执行时由实例级锁串行）
                boolean isLoopIteration = FlowTaskTypeEnum.LOOP_ITERATION.getCode().equals(task.getTaskType());
                if (!isLoopIteration && !submittedInstances.add(task.getInstanceId())) {
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

                        // 使用 task.getNodeId() 而非 instance.getCurrentNodeId() 来确定执行节点
                        FlowNode currentNode = nodes.stream()
                                .filter(n -> n.getNodeId().equals(task.getNodeId()))
                                .findFirst().orElse(null);

                        if (currentNode == null) {
                            log.warn("任务节点 {} 在流程定义中不存在，跳过执行: taskId={}",
                                    task.getNodeId(), task.getId());
                            return;
                        }

                        // 分发循环子任务/汇聚任务
                        String taskType = task.getTaskType();
                        if (FlowTaskTypeEnum.LOOP_ITERATION.getCode().equals(taskType)) {
                            flowEngine.executeLoopIterationTask(instance, task, nodes, edges);
                        } else if (FlowTaskTypeEnum.LOOP_AGGREGATE.getCode().equals(taskType)) {
                            flowEngine.executeLoopAggregateTask(instance, task, nodes, edges);
                        } else {
                            // 普通节点任务：一致性校验
                            if (!task.getNodeId().equals(instance.getCurrentNodeId())) {
                                log.warn("任务节点 {} 与实例当前节点 {} 不一致，跳过执行: instanceId={}",
                                        task.getNodeId(), instance.getCurrentNodeId(), instance.getId());
                                return;
                            }
                            flowEngine.executeNode(instance, currentNode, edges, nodes);
                        }

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
