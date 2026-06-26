package com.riverflow.admin.modules.workflow.loop;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.riverflow.admin.modules.workflow.context.FlowContext;
import com.riverflow.admin.modules.workflow.engine.FlowEngine;
import com.riverflow.admin.service.FlowInstanceService;
import com.riverflow.admin.service.FlowTaskService;
import com.riverflow.api.entity.FlowEdge;
import com.riverflow.api.entity.FlowInstance;
import com.riverflow.api.entity.FlowNode;
import com.riverflow.api.entity.FlowTask;
import com.riverflow.api.enums.FlowTaskStatusEnum;
import com.riverflow.api.enums.FlowTaskTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.riverflow.common.spring.SpringContextHolder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.riverflow.admin.modules.workflow.loop.LoopUtils.evaluateExpression;
import static com.riverflow.admin.modules.workflow.loop.LoopUtils.findNode;
import static com.riverflow.admin.modules.workflow.loop.LoopUtils.parseEndConfig;

/**
 * 异步模式下的并行循环协调器
 * <p>
 * 职责：
 * 1. 把 foreach 的 N 次迭代拆分为 N 个 LOOP_ITERATION 任务；
 * 2. 创建一个 LOOP_AGGREGATE 汇聚任务，在所有迭代完成后执行聚合；
 * 3. 提供 LOOP_ITERATION 与 LOOP_AGGREGATE 任务的执行入口。
 */
@Slf4j
@Component
public class LoopAsyncCoordinator {

    @Autowired
    private FlowTaskService flowTaskService;
    @Autowired
    private FlowInstanceService flowInstanceService;
    @Autowired
    private LoopBodyEngine loopBodyEngine;
    @Autowired
    private ObjectProvider<FlowEngine> flowEngineProvider;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private FlowEngine getFlowEngine() {
        return flowEngineProvider.getIfAvailable();
    }

    /**
     * 调度并行迭代任务
     */
    @Transactional(rollbackFor = Exception.class)
    public void scheduleParallelIterations(FlowInstance instance, FlowNode foreachNode,
                                           FlowContext context, LoopState state, LoopConfig config) {
        String endNodeId = state.getEndNodeId();
        if (endNodeId == null || endNodeId.isEmpty()) {
            throw new RuntimeException("foreach 节点未解析到结束节点: " + foreachNode.getNodeId());
        }
        String bodyEntryNodeId = state.getBodyEntryNodeId();
        if (bodyEntryNodeId == null || bodyEntryNodeId.isEmpty()) {
            throw new RuntimeException("foreach 节点未解析到循环体入口: " + foreachNode.getNodeId());
        }
        FlowNode entryNode = findNode(context.getNodes(), bodyEntryNodeId);
        FlowNode endNode = findNode(context.getNodes(), endNodeId);
        if (entryNode == null) {
            throw new RuntimeException("循环体入口节点不存在: " + bodyEntryNodeId);
        }

        String batchNo = generateBatchNo(instance.getId(), state.getLoopNodeId());
        state.setParallel(true);
        state.setBatchNo(batchNo);
        context.setGlobal(state.getStateKey(), state.toMap());

        // 创建 LOOP_ITERATION 子任务：按 parallelLimit 分批次，每批次一个任务，批次内串行执行
        Object collection = LoopUtils.evaluateCollection(state.getSourceExpr(), context);
        List<Object> itemList = collection != null ? new java.util.ArrayList<>((java.util.Collection<?>) collection) : new java.util.ArrayList<>();
        int total = itemList.size();

        // 异步并行语义：parallelLimit 表示线程数，每批大小 = floor(total / parallelLimit)
        int parallelLimit = state.getParallelLimit() > 0 ? state.getParallelLimit() : 5;
        int batchSize = total / parallelLimit;
        if (batchSize == 0) {
            batchSize = 1; // 线程数比数据量还大时，每批 1 条
        }
        int batchCount = (total + batchSize - 1) / batchSize; // 向上取整

        for (int batchIndex = 0; batchIndex < batchCount; batchIndex++) {
            FlowTask iterationTask = new FlowTask();
            iterationTask.setInstanceId(instance.getId());
            iterationTask.setNodeId(entryNode.getNodeId());
            iterationTask.setNodeName(entryNode.getNodeName());
            iterationTask.setNodeType(entryNode.getNodeType());
            iterationTask.setStatus(FlowTaskStatusEnum.PENDING.getCode());
            iterationTask.setTaskType(FlowTaskTypeEnum.LOOP_ITERATION.getCode());
            iterationTask.setBatchNo(batchNo);
            iterationTask.setLoopNodeId(state.getLoopNodeId());
            iterationTask.setIterationIndex(batchIndex); // 批次号
            iterationTask.setIsLoopInternal(1);
            iterationTask.setExecuteCount(0);
            iterationTask.setCreateTime(LocalDateTime.now());
            flowTaskService.save(iterationTask);
        }

        // 创建 LOOP_AGGREGATE 汇聚任务
        FlowTask aggregateTask = new FlowTask();
        aggregateTask.setInstanceId(instance.getId());
        aggregateTask.setNodeId(endNodeId);
        aggregateTask.setNodeName(endNode != null ? endNode.getNodeName() : "循环结束");
        aggregateTask.setNodeType(endNode != null ? endNode.getNodeType() : "end_foreach");
        aggregateTask.setStatus(FlowTaskStatusEnum.WAITING.getCode());
        aggregateTask.setTaskType(FlowTaskTypeEnum.LOOP_AGGREGATE.getCode());
        aggregateTask.setBatchNo(batchNo);
        aggregateTask.setLoopNodeId(state.getLoopNodeId());
        aggregateTask.setIsLoopInternal(1);
        aggregateTask.setExecuteCount(0);
        aggregateTask.setCreateTime(LocalDateTime.now());
        flowTaskService.save(aggregateTask);

        // 实例当前节点移动到结束节点，等待汇聚后流转
        instance.setCurrentNodeId(endNodeId);
        instance.setContextJson(context.toJsonString());
        instance.setUpdateTime(LocalDateTime.now());
        flowInstanceService.updateById(instance);

        log.info("[流程实例:{}] foreach 异步并行已调度: loopNodeId={}, batchNo={}, total={}, parallelLimit={}, batchSize={}, batchCount={}",
                instance.getId(), state.getLoopNodeId(), batchNo, total, parallelLimit, batchSize, batchCount);
    }

    /**
     * 执行单个 LOOP_ITERATION 任务
     */
    @Transactional(rollbackFor = Exception.class)
    public void executeIterationTask(FlowInstance instance, FlowTask task,
                                     List<FlowNode> nodes, List<FlowEdge> edges) {
        // 数据库乐观锁：把状态从 pending 原子地改为 running，防止调度器重复扫描或线程池重复执行同一个 task
        boolean acquired = flowTaskService.update(new UpdateWrapper<FlowTask>()
                .eq("id", task.getId())
                .eq("status", FlowTaskStatusEnum.PENDING.getCode())
                .set("status", FlowTaskStatusEnum.RUNNING.getCode())
                .set("execute_count", task.getExecuteCount() + 1)
                .set("start_time", LocalDateTime.now()));
        if (!acquired) {
            log.warn("[流程实例:{}] LOOP_ITERATION 任务已被其他线程执行或状态非法，跳过: taskId={}, status={}",
                    instance.getId(), task.getId(), task.getStatus());
            return;
        }
        task = flowTaskService.getById(task.getId());

        FlowContext mainContext = FlowContext.fromJson(instance.getContextJson());
        LoopState state = LoopState.from(mainContext.getGlobal(LoopState.key(task.getLoopNodeId())));
        if (state == null) {
            throw new RuntimeException("LOOP_ITERATION 未找到循环状态: " + task.getLoopNodeId());
        }
        if (state.getEndNodeId() == null) {
            state.setEndNodeId(LoopUtils.resolveEndLoopNodeId(
                    state.getLoopNodeId(), "foreach", nodes, edges));
        }

        FlowNode entryNode = findNode(nodes, task.getNodeId());
        if (entryNode == null) {
            throw new RuntimeException("LOOP_ITERATION 入口节点不存在: " + task.getNodeId());
        }

        int batchIndex = task.getIterationIndex() != null ? task.getIterationIndex() : 0;
        int parallelLimit = state.getParallelLimit() > 0 ? state.getParallelLimit() : 5;
        int total = state.getTotal();
        // 异步并行语义：parallelLimit 表示线程数，每批大小 = floor(total / parallelLimit)
        int batchSize = total / parallelLimit;
        if (batchSize == 0) {
            batchSize = 1;
        }
        int startIndex = batchIndex * batchSize;
        int endIndex = Math.min(startIndex + batchSize, total);

        if (startIndex < 0 || startIndex >= total) {
            log.warn("[流程实例:{}] LOOP_ITERATION 批次号越界，跳过: batchIndex={}, total={}",
                    instance.getId(), batchIndex, total);
            task.setStatus(FlowTaskStatusEnum.SUCCESS.getCode());
            task.setEndTime(LocalDateTime.now());
            flowTaskService.updateById(task);
            registerAggregateAfterCommit(instance.getId(), task.getBatchNo(), state.getLoopNodeId());
            return;
        }

        FlowContext firstContext = null;
        FlowContext lastContext = null;
        List<Object> batchResults = new ArrayList<>();
        List<Exception> errors = new ArrayList<>();

        for (int index = startIndex; index < endIndex; index++) {
            state.setIndex(index);
            // 强制根据 index 重新计算当前项；全局 state 中 currentItem 被序列化为第一项
            state.setCurrentItem(null);
            Object item = state.getCurrentItem(mainContext);
            if (item == null) {
                log.warn("[流程实例:{}] LOOP_ITERATION 无法获取当前项，跳过 index={}",
                        instance.getId(), index);
                continue;
            }

            FlowContext iterationContext = mainContext.fork();
            iterationContext.pushScope();
            iterationContext.set(state.getItemVar(), item);
            iterationContext.set(state.getIndexVar(), index);
            iterationContext.pushLoopFrame(state.getLoopNodeId(), index);
            iterationContext.setNodes(nodes);
            iterationContext.setEdges(edges);
            iterationContext.setAsyncMode(true);

            if (firstContext == null) {
                firstContext = iterationContext;
            }
            lastContext = iterationContext;

            try {
                loopBodyEngine.executeBodySync(iterationContext, entryNode, state.getEndNodeId(), nodes, edges);

                // 聚合本次迭代结果
                String aggregateExpr = resolveAggregateExpr(state, nodes);
                Object aggregateValue = null;
                if (aggregateExpr != null && !aggregateExpr.trim().isEmpty()) {
                    aggregateValue = evaluateExpression(aggregateExpr, iterationContext);
                }
                // 结果写入 Redis（按 index 分片）
                boolean saved = saveIterationResult(instance.getId(), state.getLoopNodeId(), index, aggregateValue);
                if (!saved) {
                    throw new RuntimeException("迭代结果写入 Redis 失败，触发重试: index=" + index);
                }
                // 内存中也保留一份，作为 Redis 不可用时降级
                state.setResult(index, aggregateValue);
                batchResults.add(aggregateValue);

                log.info("[流程实例:{}] LOOP_ITERATION 批次内元素完成: batchNo={}, batchIndex={}, index={}/{}",
                        instance.getId(), task.getBatchNo(), batchIndex, index + 1, total);
            } catch (Exception e) {
                log.error("[流程实例:{}] LOOP_ITERATION 批次内元素执行失败: batchNo={}, batchIndex={}, index={}",
                        instance.getId(), task.getBatchNo(), batchIndex, index, e);
                errors.add(e);
                if (!state.isContinueOnFail()) {
                    break;
                }
            }
        }

        if (firstContext != null) {
            task.setInputContext(firstContext.toJsonString());
        }
        if (lastContext != null) {
            task.setOutputContext(lastContext.toJsonString());
        }
        task.setResultJson(JSON.toJSONString(batchResults));

        if (!errors.isEmpty()) {
            task.setStatus(FlowTaskStatusEnum.FAIL.getCode());
            task.setEndTime(LocalDateTime.now());
            task.setErrorMsg(errors.get(0).getMessage());
            flowTaskService.updateById(task);
            log.error("[流程实例:{}] LOOP_ITERATION 批次失败: batchNo={}, batchIndex={}, errors={}",
                    instance.getId(), task.getBatchNo(), batchIndex, errors.size());
            // 不抛出异常，避免事务回滚导致任务状态丢失
        } else {
            task.setStatus(FlowTaskStatusEnum.SUCCESS.getCode());
            task.setEndTime(LocalDateTime.now());
            flowTaskService.updateById(task);

            log.info("[流程实例:{}] LOOP_ITERATION 批次完成: batchNo={}, batchIndex={}, batchSize={}",
                    instance.getId(), task.getBatchNo(), batchIndex, endIndex - startIndex);
        }

        // 无论成功失败都尝试触发汇聚，避免最后一个批次失败导致流程卡住
        registerAggregateAfterCommit(instance.getId(), task.getBatchNo(), state.getLoopNodeId());
    }

    /**
     * 注册事务提交后的汇聚触发回调
     * <p>
     * 当前 LOOP_ITERATION 任务的事务提交后，再开启新事务查询所有批次状态，
     * 避免同一事务中读不到其他线程已提交的迭代结果。
     */
    private void registerAggregateAfterCommit(Long instanceId, String batchNo, String loopNodeId) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            // 非事务场景（理论上不会出现）直接调用作为兜底
            tryAggregate(instanceId, batchNo, loopNodeId);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    LoopAsyncCoordinator coordinator = SpringContextHolder.getBean(LoopAsyncCoordinator.class);
                    coordinator.tryAggregate(instanceId, batchNo, loopNodeId);
                } catch (Exception e) {
                    log.error("[流程实例:{}] 事务提交后触发汇聚失败: batchNo={}", instanceId, batchNo, e);
                }
            }
        });
    }

    /**
     * 尝试触发 LOOP_AGGREGATE 任务
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void tryAggregate(Long instanceId, String batchNo, String loopNodeId) {
        FlowInstance instance = flowInstanceService.getById(instanceId);
        if (instance == null) {
            log.warn("[流程实例:{}] 汇聚时未找到流程实例", instanceId);
            return;
        }
        // 查询同一 batch 下的 LOOP_ITERATION 任务，排除 LOOP_AGGREGATE 自身
        List<FlowTask> allBatchTasks = flowTaskService.listByInstanceIdAndBatchNo(instance.getId(), batchNo);
        if (allBatchTasks == null || allBatchTasks.isEmpty()) {
            return;
        }
        List<FlowTask> iterations = allBatchTasks.stream()
                .filter(t -> FlowTaskTypeEnum.LOOP_ITERATION.getCode().equals(t.getTaskType()))
                .toList();
        List<FlowTask> aggregates = allBatchTasks.stream()
                .filter(t -> FlowTaskTypeEnum.LOOP_AGGREGATE.getCode().equals(t.getTaskType()))
                .toList();

        if (iterations.isEmpty()) {
            return;
        }

        boolean allDone = iterations.stream()
                .allMatch(t -> FlowTaskStatusEnum.SUCCESS.getCode().equals(t.getStatus())
                        || FlowTaskStatusEnum.FAIL.getCode().equals(t.getStatus()));
        if (!allDone) {
            log.debug("[流程实例:{}] 并行迭代尚未全部完成，暂不汇聚: batchNo={}, done={}/{}",
                    instance.getId(), batchNo,
                    iterations.stream().filter(t -> FlowTaskStatusEnum.SUCCESS.getCode().equals(t.getStatus())
                            || FlowTaskStatusEnum.FAIL.getCode().equals(t.getStatus())).count(),
                    iterations.size());
            return;
        }

        log.info("[流程实例:{}] 并行迭代全部完成，准备激活汇聚任务: batchNo={}, iterationCount={}",
                instance.getId(), batchNo, iterations.size());

        if (aggregates.isEmpty()) {
            log.warn("[流程实例:{}] 未找到 LOOP_AGGREGATE 任务: batchNo={}", instance.getId(), batchNo);
            return;
        }
        for (FlowTask aggregate : aggregates) {
            if (FlowTaskStatusEnum.WAITING.getCode().equals(aggregate.getStatus())) {
                aggregate.setStatus(FlowTaskStatusEnum.PENDING.getCode());
                aggregate.setNextExecuteTime(null);
                flowTaskService.updateById(aggregate);
                log.info("[流程实例:{}] 激活汇聚任务: batchNo={}, aggregateTaskId={}",
                        instance.getId(), batchNo, aggregate.getId());
            }
        }
    }

    /**
     * 执行 LOOP_AGGREGATE 任务
     */
    @Transactional(rollbackFor = Exception.class)
    public void executeAggregateTask(FlowInstance instance, FlowTask task,
                                     List<FlowNode> nodes, List<FlowEdge> edges) {
        // 数据库乐观锁：防止多个线程同时执行同一个 LOOP_AGGREGATE
        boolean acquired = flowTaskService.update(new UpdateWrapper<FlowTask>()
                .eq("id", task.getId())
                .eq("status", FlowTaskStatusEnum.PENDING.getCode())
                .set("status", FlowTaskStatusEnum.RUNNING.getCode())
                .set("execute_count", task.getExecuteCount() + 1)
                .set("start_time", LocalDateTime.now()));
        if (!acquired) {
            log.warn("[流程实例:{}] LOOP_AGGREGATE 任务已被其他线程执行或状态非法，跳过: taskId={}, status={}",
                    instance.getId(), task.getId(), task.getStatus());
            return;
        }
        task = flowTaskService.getById(task.getId());

        FlowContext context = FlowContext.fromJson(instance.getContextJson());
        LoopState state = LoopState.from(context.getGlobal(LoopState.key(task.getLoopNodeId())));
        if (state == null) {
            throw new RuntimeException("LOOP_AGGREGATE 未找到循环状态: " + task.getLoopNodeId());
        }

        FlowNode endNode = findNode(nodes, task.getNodeId());
        if (endNode == null) {
            throw new RuntimeException("LOOP_AGGREGATE 结束节点不存在: " + task.getNodeId());
        }

        // 优先从 Redis 收集所有 iteration 结果；Redis 不可用时降级到 state.getResults()
        List<Object> results = collectIterationResults(instance.getId(), state);
        if (results == null || results.isEmpty()) {
            results = state.getResults();
        }
        // 结果变量写入全局上下文
        context.setGlobal(state.getResultVar(), results);

        // 应用 outputMapping
        EndLoopConfig endConfig = parseEndConfig(endNode.getConfigJson());
        applyOutputMapping(endConfig, context, state);

        // 清理循环状态
        context.removeGlobal(state.getStateKey());
        context.removeGlobal(state.getStartTimeKey());
        // 清理 Redis 中的迭代结果
        clearIterationResults(instance.getId(), state.getLoopNodeId(), state.getTotal());
        while (context.inScope()) {
            context.popScope();
        }
        while (context.getCurrentLoopFrame() != null) {
            context.popLoopFrame();
        }

        instance.setContextJson(context.toJsonString());
        instance.setCurrentNodeId(endNode.getNodeId());
        instance.setUpdateTime(LocalDateTime.now());
        flowInstanceService.updateById(instance);

        task.setStatus(FlowTaskStatusEnum.SUCCESS.getCode());
        task.setEndTime(LocalDateTime.now());
        task.setOutputContext(context.toJsonString());
        flowTaskService.updateById(task);

        log.info("[流程实例:{}] LOOP_AGGREGATE 完成: loopNodeId={}, batchNo={}, resultVar={}",
                instance.getId(), state.getLoopNodeId(), task.getBatchNo(), state.getResultVar());

        // 继续向后流转
        FlowEngine flowEngine = getFlowEngine();
        if (flowEngine != null) {
            flowEngine.transitionAfterLoop(instance, endNode, edges, nodes, context);
        } else {
            log.error("[流程实例:{}] LOOP_AGGREGATE 完成后未找到 FlowEngine，无法继续流转", instance.getId());
        }
    }

    private String resolveAggregateExpr(LoopState state, List<FlowNode> nodes) {
        FlowNode endNode = findNode(nodes, state.getEndNodeId());
        if (endNode == null) {
            return null;
        }
        EndLoopConfig endConfig = parseEndConfig(endNode.getConfigJson());
        if (endConfig.getAggregateExpr() != null && !endConfig.getAggregateExpr().trim().isEmpty()) {
            return endConfig.getAggregateExpr();
        }
        return "context." + state.getItemVar();
    }

    private void applyOutputMapping(EndLoopConfig config, FlowContext context, LoopState state) {
        List<java.util.Map<String, String>> outputMapping = config.getOutputMapping();
        if (outputMapping == null || outputMapping.isEmpty()) {
            return;
        }
        for (java.util.Map<String, String> mapping : outputMapping) {
            if (mapping == null) {
                continue;
            }
            String source = mapping.get("source");
            String target = mapping.get("target");
            if (source == null || target == null) {
                continue;
            }
            Object value = context.getByPath(source);
            if (value != null) {
                String key = target.startsWith("context.") ? target.substring(8) : target;
                context.setGlobal(key, value);
            }
        }
    }

    private String generateBatchNo(Long instanceId, String loopNodeId) {
        return instanceId + "_" + loopNodeId + "_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    /**
     * 迭代结果 Redis key
     */
    private String resultKey(Long instanceId, String loopNodeId, int index) {
        return "loop:result:" + instanceId + ":" + loopNodeId + ":" + index;
    }

    /**
     * 迭代状态 Redis key
     */
    private String statusKey(Long instanceId, String loopNodeId, int index) {
        return "loop:status:" + instanceId + ":" + loopNodeId + ":" + index;
    }

    /**
     * 保存单次迭代结果到 Redis
     *
     * @return 是否保存成功
     */
    private boolean saveIterationResult(Long instanceId, String loopNodeId, int index, Object value) {
        try {
            String resultKey = resultKey(instanceId, loopNodeId, index);
            String statusKey = statusKey(instanceId, loopNodeId, index);
            String jsonValue = value != null ? JSON.toJSONString(value) : "null";
            redisTemplate.opsForValue().set(resultKey, jsonValue, 30, TimeUnit.MINUTES);
            redisTemplate.opsForValue().set(statusKey, "SUCCESS", 30, TimeUnit.MINUTES);
            return true;
        } catch (Exception e) {
            log.error("[流程实例:{}] 保存迭代结果到 Redis 失败: loopNodeId={}, index={}",
                    instanceId, loopNodeId, index, e);
            return false;
        }
    }

    /**
     * 从 Redis 收集所有迭代结果
     *
     * @return 结果列表；如果有任一迭代未完成或 Redis 异常，返回 null 以触发降级
     */
    private List<Object> collectIterationResults(Long instanceId, LoopState state) {
        if (state == null || !state.isForeach() || state.getTotal() <= 0) {
            return null;
        }
        try {
            List<Object> results = new ArrayList<>(state.getTotal());
            for (int i = 0; i < state.getTotal(); i++) {
                String statusKey = statusKey(instanceId, state.getLoopNodeId(), i);
                String status = redisTemplate.opsForValue().get(statusKey);
                if (!"SUCCESS".equals(status)) {
                    log.warn("[流程实例:{}] 迭代 {} 状态未就绪，暂不汇聚", instanceId, i);
                    return null;
                }
                String resultKey = resultKey(instanceId, state.getLoopNodeId(), i);
                String value = redisTemplate.opsForValue().get(resultKey);
                results.add("null".equals(value) ? null : JSON.parse(value));
            }
            return results;
        } catch (Exception e) {
            log.error("[流程实例:{}] 从 Redis 收集迭代结果失败，降级到内存 results", instanceId, e);
            return null;
        }
    }

    /**
     * 清理 Redis 中的迭代结果
     */
    private void clearIterationResults(Long instanceId, String loopNodeId, int total) {
        try {
            for (int i = 0; i < total; i++) {
                redisTemplate.delete(resultKey(instanceId, loopNodeId, i));
                redisTemplate.delete(statusKey(instanceId, loopNodeId, i));
            }
        } catch (Exception e) {
            log.warn("[流程实例:{}] 清理 Redis 迭代结果失败: loopNodeId={}", instanceId, loopNodeId, e);
        }
    }
}
