package com.riverflow.admin.modules.workflow.scheduler;

import com.riverflow.admin.infra.util.NodeIdUtil;
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
import com.riverflow.api.enums.FlowTaskTypeEnum;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 流程调度器
 *
 * <p>定时扫描待执行的流程任务并驱动执行。</p>
 * <ul>
 *   <li>使用 Redisson 分布式锁保证集群内只有一个节点执行调度扫描；</li>
 *   <li>扫描后通过数据库乐观锁“认领”任务，防止同节点多线程或分布式锁极端情况下重复执行；</li>
 *   <li>同一轮扫描中，同一个实例（非 LOOP_ITERATION）只提交一次，避免重复提交到线程池。</li>
 * </ul>
 */
@Slf4j
@Component
public class FlowScheduler {

    private static final String SCHEDULER_LOCK_KEY = "riverflow:scheduler:lock:scanPendingTasks";

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
    @Autowired
    private RedissonClient redissonClient;

    @Value("${riverflow.distributed.scheduler-fixed-rate:10000}")
    private long schedulerFixedRate;

    @Value("${riverflow.scheduler.scan-limit:100}")
    private int scanLimit;

    @Value("${riverflow.distributed.scheduler-lock-at-most-seconds:9}")
    private long schedulerLockAtMostSeconds;

    @Value("${riverflow.distributed.scheduler-lock-wait-time-ms:0}")
    private long schedulerLockWaitTimeMs;

    @Autowired
    private MeterRegistry meterRegistry;

    private Counter scanCounter;
    private Counter claimedCounter;
    private Counter skippedCounter;

    @Autowired
    public void initMetrics() {
        this.scanCounter = Counter.builder("flow_scheduler_scan_total")
                .description("调度器扫描次数")
                .register(meterRegistry);
        this.claimedCounter = Counter.builder("flow_scheduler_claimed_total")
                .description("调度器成功认领任务数")
                .register(meterRegistry);
        this.skippedCounter = Counter.builder("flow_scheduler_skipped_total")
                .description("调度器认领失败任务数")
                .register(meterRegistry);
    }

    /**
     * 每10秒扫描一次待执行任务，提交到线程池异步执行
     *
     * <p>使用 Redisson 分布式锁保证同一时刻只有一个节点执行扫描。</p>
     */
    @Scheduled(fixedRateString = "${riverflow.distributed.scheduler-fixed-rate:10000}")
    public void scanPendingTasks() {
        String nodeId = NodeIdUtil.getNodeId();
        RLock lock = redissonClient.getLock(SCHEDULER_LOCK_KEY);
        boolean locked = false;
        try {
            // leaseTime <= 0 启用 Redisson 看门狗自动续期，业务完成后 finally 释放
            // 看门狗默认每 10 秒续期一次，保持锁 30 秒有效，直到 unlock() 被调用
            long leaseTime = schedulerLockAtMostSeconds > 0 ? schedulerLockAtMostSeconds : -1;
            locked = lock.tryLock(schedulerLockWaitTimeMs, leaseTime, TimeUnit.SECONDS);
            if (!locked) {
                log.info("[调度器][{}] 未获取到分布式锁，跳过本次扫描", nodeId);
                return;
            }
            log.info("[调度器][{}] 获取到分布式锁，开始扫描任务", nodeId);
            doScan();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("[调度器][{}] 获取锁被中断", nodeId, e);
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("[调度器][{}] 释放分布式锁", nodeId);
            }
        }
    }

    private void doScan() {
        try {
            scanCounter.increment();
            List<FlowTask> pendingTasks = flowTaskService.getPendingTasks(LocalDateTime.now(), scanLimit);
            if (pendingTasks == null || pendingTasks.isEmpty()) {
                return;
            }

            log.debug("[调度器] 扫描到 {} 个待执行任务", pendingTasks.size());

            // 同一轮扫描中，同一个实例只提交一次（避免重复提交到线程池）
            Set<Long> submittedInstances = ConcurrentHashMap.newKeySet();
            String nodeId = NodeIdUtil.getNodeId();
            List<FlowTask> claimedTasks = new ArrayList<>();

            for (FlowTask task : pendingTasks) {
                boolean isLoopIteration = FlowTaskTypeEnum.LOOP_ITERATION.getCode().equals(task.getTaskType());
                if (!isLoopIteration && !submittedInstances.add(task.getInstanceId())) {
                    continue;
                }

                boolean claimed = flowTaskService.claimTask(task.getId(), task.getVersion(), nodeId);
                if (!claimed) {
                    skippedCounter.increment();
                    log.debug("[调度器] 任务认领失败，可能已被其他线程处理: taskId={}", task.getId());
                    continue;
                }
                claimedCounter.increment();
                task.setStatus("running");
                task.setVersion(task.getVersion() + 1);
                task.setExecuteNode(nodeId);
                claimedTasks.add(task);
            }

            if (claimedTasks.isEmpty()) {
                return;
            }
            log.debug("[调度器] 成功认领 {} 个任务，准备提交执行器", claimedTasks.size());

            for (FlowTask task : claimedTasks) {
                flowExecutor.submit(() -> executeTask(task));
            }
        } catch (Exception e) {
            log.error("[调度器] 扫描待执行任务异常", e);
        }
    }

    private void executeTask(FlowTask task) {
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
    }
}
