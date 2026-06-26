package com.riverflow.admin.modules.workflow.loop;

import com.riverflow.admin.modules.workflow.context.FlowContext;
import com.riverflow.admin.modules.workflow.engine.NodeExecuteResult;
import com.riverflow.admin.modules.workflow.node.NodeExecutor;
import com.riverflow.api.entity.FlowNode;
import com.riverflow.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.riverflow.admin.modules.workflow.loop.ForeachNodeExecutor.pushIterationScope;
import static com.riverflow.admin.modules.workflow.loop.LoopUtils.evaluateExpression;
import static com.riverflow.admin.modules.workflow.loop.LoopUtils.parseEndConfig;

/**
 * end_foreach 循环结束节点执行器
 */
@Slf4j
@Component
public class EndForeachNodeExecutor implements NodeExecutor {

    @Override
    public String getNodeType() {
        return "end_foreach";
    }

    @Override
    public NodeExecuteResult execute(FlowNode node, FlowContext context) {
        log.info("[流程实例:{}] 执行 end_foreach 节点: {}", context.getInstanceId(), node.getNodeName());

        EndLoopConfig config = parseEndConfig(node.getConfigJson());
        String loopNodeId = config.getLoopNodeId();
        if (loopNodeId == null || loopNodeId.trim().isEmpty()) {
            throw new BusinessException("end_foreach 节点未配置关联的循环节点");
        }

        LoopState state = LoopState.from(context.getGlobal(LoopState.key(loopNodeId)));
        if (state == null) {
            throw new BusinessException("end_foreach 未找到关联的循环状态: " + loopNodeId);
        }

        // 校验超时
        checkTimeout(state, context);

        // 判断 break 条件
        if (shouldBreak(state, config, context)) {
            log.info("[流程实例:{}] foreach 循环触发 break，退出循环: {}",
                    context.getInstanceId(), node.getNodeName());
            return exitLoop(state, context, config);
        }

        // 记录循环维度信息，pop 后无法从上下文获取
        final Integer iterationIndex = state.getIndex();

        // 幂等：当前 index 已聚合过则跳过
        if (!state.isCurrentIndexAggregated()) {
            Object aggregateValue = evaluateAggregate(config.getAggregateExpr(), context);
            state.addResult(aggregateValue);
            state.markCurrentIndexAggregated();
            context.setGlobal(state.getStateKey(), state.toMap());
            log.info("[流程实例:{}] foreach 第 {}/{} 次迭代聚合完成",
                    context.getInstanceId(), state.getIndex() + 1, state.getTotal());
        } else {
            log.info("[流程实例:{}] foreach 第 {}/{} 次迭代已聚合，幂等跳过",
                    context.getInstanceId(), state.getIndex() + 1, state.getTotal());
        }

        // 弹出当前迭代作用域
        context.popScope();
        context.popLoopFrame();

        // 判断是否满足退出条件
        if (state.getIndex() + 1 >= state.getTotal()) {
            log.info("[流程实例:{}] foreach 循环执行完毕，退出循环",
                    context.getInstanceId());
            return exitLoop(state, context, config);
        }

        // 继续下一次迭代
        state.nextIndex(context);
        state.clearAggregatedFlag();
        context.setGlobal(state.getStateKey(), state.toMap());
        pushIterationScope(context, state);

        return NodeExecuteResult.success()
                .withNextEntryNode(state.getBodyEntryNodeId(), loopNodeId, iterationIndex);
    }

    /**
     * 判断 break 条件
     */
    private boolean shouldBreak(LoopState state, EndLoopConfig config, FlowContext context) {
        String breakExpr = state.getBreakExpr();
        if (breakExpr == null || breakExpr.trim().isEmpty()) {
            return false;
        }
        return LoopUtils.evaluateBoolean(breakExpr, context);
    }

    /**
     * 聚合本次迭代结果
     */
    private Object evaluateAggregate(String aggregateExpr, FlowContext context) {
        if (aggregateExpr == null || aggregateExpr.trim().isEmpty()) {
            // 默认聚合当前 item
            return null;
        }
        return evaluateExpression(aggregateExpr, context);
    }

    /**
     * 退出循环
     */
    private NodeExecuteResult exitLoop(LoopState state, FlowContext context, EndLoopConfig config) {
        // 聚合结果写入全局上下文
        context.setGlobal(state.getResultVar(), state.getResults());

        // 应用 outputMapping
        applyOutputMapping(config, context, state);

        // 清理循环状态
        context.removeGlobal(state.getStateKey());
        context.removeGlobal(state.getStartTimeKey());

        // 确保作用域和循环帧已清空
        while (context.inScope()) {
            context.popScope();
        }
        while (context.getCurrentLoopFrame() != null) {
            context.popLoopFrame();
        }

        log.info("[流程实例:{}] foreach 循环退出，结果变量: {}, 结果数: {}",
                context.getInstanceId(), state.getResultVar(), state.getResults().size());

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
     * 校验超时
     */
    private void checkTimeout(LoopState state, FlowContext context) {
        Object startTimeObj = context.getGlobal(state.getStartTimeKey());
        if (startTimeObj == null) {
            return;
        }
        long startTime = Long.parseLong(startTimeObj.toString());
        // 默认超时 30 秒
        long timeout = 30000L;
        Object loopStateObj = context.getGlobal(state.getStateKey());
        if (loopStateObj instanceof Map) {
            Object timeoutObj = ((Map<?, ?>) loopStateObj).get("timeout");
            if (timeoutObj != null) {
                timeout = Long.parseLong(timeoutObj.toString());
            }
        }
        if (System.currentTimeMillis() - startTime > timeout) {
            throw new BusinessException("foreach 循环执行超时");
        }
    }
}
