package com.riverflow.admin.modules.workflow.loop;

import com.riverflow.admin.modules.workflow.context.FlowContext;
import com.riverflow.admin.modules.workflow.engine.NodeExecuteResult;
import com.riverflow.admin.modules.workflow.node.NodeExecutor;
import com.riverflow.admin.service.FlowInstanceService;
import com.riverflow.api.entity.FlowInstance;
import com.riverflow.api.entity.FlowNode;
import com.riverflow.common.constant.CommonConstant;
import com.riverflow.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.riverflow.admin.modules.workflow.loop.LoopUtils.evaluateCollection;
import static com.riverflow.admin.modules.workflow.loop.LoopUtils.parseConfig;
import static com.riverflow.admin.modules.workflow.loop.LoopUtils.resolveBodyEntryNodeId;
import static com.riverflow.admin.modules.workflow.loop.LoopUtils.resolveEndLoopNodeId;

/**
 * foreach 循环开始节点执行器
 */
@Slf4j
@Component
public class ForeachNodeExecutor implements NodeExecutor {

    @Autowired
    private LoopBodyEngine loopBodyEngine;

    @Autowired
    @Qualifier("flowExecutor")
    private Executor loopExecutor;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private FlowInstanceService flowInstanceService;

    @Autowired
    private LoopAsyncCoordinator loopAsyncCoordinator;

    private final ScheduledExecutorService lockRenewExecutor = Executors.newScheduledThreadPool(2, r -> {
        Thread t = new Thread(r, "loop-lock-renew");
        t.setDaemon(true);
        return t;
    });

    @Override
    public String getNodeType() {
        return "foreach";
    }

    @Override
    public NodeExecuteResult execute(FlowNode node, FlowContext context) {
        log.info("[流程实例:{}] >>> 进入 ForeachNodeExecutor.execute: nodeId={}, nodeName={}",
                context.getInstanceId(), node.getNodeId(), node.getNodeName());

        LoopConfig config = parseConfig(node.getConfigJson());
        log.info("[流程实例:{}] foreach 配置解析完成: sourceExpr={}, itemVar={}, indexVar={}, resultVar={}, parallel={}",
                context.getInstanceId(), config.getSourceExpr(), config.getItemVar(),
                config.getIndexVar(), config.getResultVar(), config.getParallel());
        if (config.getSourceExpr() == null || config.getSourceExpr().trim().isEmpty()) {
            throw new BusinessException("foreach 节点缺少循环源表达式");
        }

        // 幂等：检查是否已初始化
        Object existingStateObj = context.getGlobal(LoopState.key(node.getNodeId()));
        log.info("[流程实例:{}] foreach 已有状态对象: {}", context.getInstanceId(), existingStateObj != null);
        LoopState existing = LoopState.from(existingStateObj);
        if (existing != null && existing.isInitialized()) {
            log.info("[流程实例:{}] foreach 节点已初始化，幂等跳过: {}",
                    context.getInstanceId(), node.getNodeName());
            if (existing.getBodyEntryNodeId() == null) {
                existing.setBodyEntryNodeId(resolveBodyEntryNodeId(node.getNodeId(), context.getNodes(), context.getEdges()));
            }
            pushIterationScope(context, existing);
            return NodeExecuteResult.success()
                    .withNextEntryNode(existing.getBodyEntryNodeId(), existing.getLoopNodeId(), existing.getIndex());
        }

        // 求值循环源
        log.info("[流程实例:{}] foreach 开始求值循环源: {}", context.getInstanceId(), config.getSourceExpr());
        Collection<?> items = evaluateCollection(config.getSourceExpr(), context);
        log.info("[流程实例:{}] foreach 循环源求值完成: size={}", context.getInstanceId(), items == null ? 0 : items.size());

        // 空集合处理
        if (items == null || items.isEmpty()) {
            return handleEmpty(node, config, context);
        }

        // 校验最大迭代数
        int maxIterations = config.getMaxIterations() != null ? config.getMaxIterations() : 100;
        if (items.size() > maxIterations) {
            throw new BusinessException("循环次数超过最大限制: " + maxIterations);
        }

        // 初始化循环状态
        log.info("[流程实例:{}] foreach 开始初始化 LoopState", context.getInstanceId());
        LoopState state = new LoopState(node.getNodeId(), items, config);
        log.info("[流程实例:{}] foreach LoopState 创建完成, total={}", context.getInstanceId(), state.getTotal());
        String bodyEntryNodeId = resolveBodyEntryNodeId(node.getNodeId(), context.getNodes(), context.getEdges());
        String endNodeId = resolveEndLoopNodeId(node.getNodeId(), node.getNodeType(), context.getNodes(), context.getEdges());
        log.info("[流程实例:{}] foreach 图解析完成: bodyEntryNodeId={}, endNodeId={}",
                context.getInstanceId(), bodyEntryNodeId, endNodeId);
        state.setBodyEntryNodeId(bodyEntryNodeId);
        state.setEndNodeId(endNodeId);
        state.setInitialized(true);
        state.setStartTime(System.currentTimeMillis());
        state.setParallel(Boolean.TRUE.equals(config.getParallel()));

        log.info("[流程实例:{}] foreach 开始保存 LoopState 到 context", context.getInstanceId());
        context.setGlobal(state.getStateKey(), state.toMap());
        context.setGlobal(state.getStartTimeKey(), state.getStartTime());
        log.info("[流程实例:{}] foreach LoopState 保存完成", context.getInstanceId());

        // 同步模式下的并行迭代
        boolean parallel = Boolean.TRUE.equals(config.getParallel());
        if (parallel && loopExecutor != null && !context.isAsyncMode()) {
            return executeParallel(node, context, state, config);
        }

        // 异步模式下的并行迭代：拆分为 LOOP_ITERATION 子任务 + LOOP_AGGREGATE 汇聚任务
        if (parallel && context.isAsyncMode()) {
            FlowInstance instance = flowInstanceService.getById(context.getInstanceId());
            if (existing != null && existing.isParallel()) {
                log.info("[流程实例:{}] foreach 并行迭代已调度，幂等跳过", context.getInstanceId());
                return NodeExecuteResult.success().withTerminateChain(true);
            }
            if (instance != null) {
                loopAsyncCoordinator.scheduleParallelIterations(instance, node, context, state, config);
                log.info("[流程实例:{}] foreach 节点进入异步并行调度，共 {} 项", context.getInstanceId(), state.getTotal());
                return NodeExecuteResult.success().withTerminateChain(true);
            }
        }

        // 串行模式：第一次迭代
        log.info("[流程实例:{}] foreach 开始 pushIterationScope", context.getInstanceId());
        pushIterationScope(context, state);
        log.info("[流程实例:{}] foreach pushIterationScope 完成, item={}, index={}",
                context.getInstanceId(), state.getCurrentItem(context), state.getIndex());

        log.info("[流程实例:{}] foreach 节点初始化完成（串行），共 {} 项，入口节点: {}, loopNodeId={}",
                context.getInstanceId(), state.getTotal(), state.getBodyEntryNodeId(), state.getLoopNodeId());

        NodeExecuteResult result = NodeExecuteResult.success()
                .withNextEntryNode(state.getBodyEntryNodeId(), state.getLoopNodeId(), state.getIndex());
        log.info("[流程实例:{}] <<< 离开 ForeachNodeExecutor.execute, nextEntryNodeId={}",
                context.getInstanceId(), result.getNextEntryNodeId());
        return result;
    }

    /**
     * 并行执行迭代
     */
    private NodeExecuteResult executeParallel(FlowNode node, FlowContext context,
                                              LoopState state, LoopConfig config) {
        log.info("[流程实例:{}] foreach 节点进入并行模式，共 {} 项，并行度: {}",
                context.getInstanceId(), state.getTotal(), config.getParallelLimit());

        String endNodeId = LoopUtils.resolveEndLoopNodeId(
                node.getNodeId(), node.getNodeType(), context.getNodes(), context.getEdges());
        FlowNode endNode = LoopUtils.findNode(context.getNodes(), endNodeId);
        EndLoopConfig endConfig = endNode != null ? LoopUtils.parseEndConfig(endNode.getConfigJson()) : new EndLoopConfig();
        final String aggregateExpr;
        if (endConfig.getAggregateExpr() == null || endConfig.getAggregateExpr().trim().isEmpty()) {
            aggregateExpr = "context." + state.getItemVar();
        } else {
            aggregateExpr = endConfig.getAggregateExpr();
        }

        int parallelLimit = config.getParallelLimit() != null && config.getParallelLimit() > 0
                ? config.getParallelLimit() : 5;

        List<Object> results = new ArrayList<>();
        Object collection = LoopUtils.evaluateCollection(state.getSourceExpr(), context);
        List<Object> itemList = collection != null ? new ArrayList<>((Collection<?>) collection) : new ArrayList<>();

        // 锁续期（异步模式下防止长循环导致锁过期）
        String lockKey = CommonConstant.FLOW_LOCK_PREFIX + context.getInstanceId();
        ScheduledFuture<?> renewFuture = startLockRenew(lockKey);

        try {
            for (int batchStart = 0; batchStart < itemList.size(); batchStart += parallelLimit) {
                int batchEnd = Math.min(batchStart + parallelLimit, itemList.size());
                List<CompletableFuture<Object>> futures = new ArrayList<>();
                CountDownLatch latch = new CountDownLatch(batchEnd - batchStart);
                AtomicInteger failCount = new AtomicInteger(0);

                for (int i = batchStart; i < batchEnd; i++) {
                    final int index = i;
                    final Object item = itemList.get(index);
                    futures.add(CompletableFuture.supplyAsync(() -> {
                        try {
                            FlowContext iterationContext = context.fork();
                            iterationContext.pushScope();
                            iterationContext.set(state.getItemVar(), item);
                            iterationContext.set(state.getIndexVar(), index);

                            FlowNode entryNode = LoopUtils.findNode(context.getNodes(), state.getBodyEntryNodeId());
                            if (entryNode == null) {
                                throw new BusinessException("循环体入口节点不存在: " + state.getBodyEntryNodeId());
                            }

                            loopBodyEngine.executeBodySync(iterationContext, entryNode, endNodeId,
                                    context.getNodes(), context.getEdges());

                            return LoopUtils.evaluateExpression(aggregateExpr, iterationContext);
                        } catch (Exception e) {
                            log.error("[流程实例:{}] 并行迭代 {} 执行失败", context.getInstanceId(), index, e);
                            failCount.incrementAndGet();
                            if (!Boolean.TRUE.equals(config.getContinueOnFail())) {
                                throw new RuntimeException("并行迭代执行失败: " + e.getMessage(), e);
                            }
                            return null;
                        } finally {
                            latch.countDown();
                        }
                    }, loopExecutor));
                }

                // 等待本批完成
                latch.await();

                // 保持结果顺序
                for (CompletableFuture<Object> future : futures) {
                    results.add(future.join());
                }

                if (failCount.get() > 0 && !Boolean.TRUE.equals(config.getContinueOnFail())) {
                    throw new BusinessException("并行迭代中存在失败项");
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("并行迭代被中断: " + e.getMessage());
        } finally {
            renewFuture.cancel(false);
        }

        // 写入聚合结果
        context.setGlobal(state.getResultVar(), results);
        applyOutputMapping(endConfig, context, state);

        // 清理循环状态
        context.removeGlobal(state.getStateKey());
        context.removeGlobal(state.getStartTimeKey());

        log.info("[流程实例:{}] foreach 并行循环完成，结果数: {}",
                context.getInstanceId(), results.size());

        return NodeExecuteResult.success().withExitLoop(true);
    }

    /**
     * 启动锁续期任务
     */
    private ScheduledFuture<?> startLockRenew(String lockKey) {
        try {
            return lockRenewExecutor.scheduleAtFixedRate(() -> {
                try {
                    redisTemplate.expire(lockKey, 30, TimeUnit.SECONDS);
                } catch (Exception e) {
                    log.warn("锁续期失败: {}", lockKey, e);
                }
            }, 10, 10, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("启动锁续期任务失败", e);
            return null;
        }
    }

    /**
     * 空集合处理
     */
    private NodeExecuteResult handleEmpty(FlowNode node, LoopConfig config, FlowContext context) {
        String emptyAction = config.getEmptyAction();
        if (emptyAction == null) {
            emptyAction = "skip";
        }
        log.info("[流程实例:{}] foreach 节点循环源为空，处理方式: {}",
                context.getInstanceId(), emptyAction);

        if ("fail".equals(emptyAction)) {
            throw new BusinessException("foreach 循环源为空: " + node.getNodeName());
        }
        return NodeExecuteResult.success().withExitLoop(true);
    }

    /**
     * 应用输出映射
     */
    private void applyOutputMapping(EndLoopConfig config, FlowContext context, LoopState state) {
        List<Map<String, String>> outputMapping = config.getOutputMapping();
        if (outputMapping == null || outputMapping.isEmpty()) {
            return;
        }
        for (Map<String, String> mapping : outputMapping) {
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

    /**
     * 将本次迭代的变量压入作用域
     */
    static void pushIterationScope(FlowContext context, LoopState state) {
        context.pushScope();
        if (state.isForeach()) {
            context.set(state.getItemVar(), state.getCurrentItem(context));
            context.set(state.getIndexVar(), state.getIndex());
            context.pushLoopFrame(state.getLoopNodeId(), state.getIndex());
        } else {
            context.pushLoopFrame(state.getLoopNodeId(), state.getIterationCount());
        }
    }
}
