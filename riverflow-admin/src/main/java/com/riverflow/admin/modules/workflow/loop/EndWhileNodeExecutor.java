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

import static com.riverflow.admin.modules.workflow.loop.LoopUtils.evaluateBoolean;
import static com.riverflow.admin.modules.workflow.loop.LoopUtils.parseEndConfig;

/**
 * end_while 循环结束节点执行器
 */
@Slf4j
@Component
public class EndWhileNodeExecutor implements NodeExecutor {

    @Override
    public String getNodeType() {
        return "end_while";
    }

    @Override
    public NodeExecuteResult execute(FlowNode node, FlowContext context) {
        log.info("[流程实例:{}] 执行 end_while 节点: {}", context.getInstanceId(), node.getNodeName());

        EndLoopConfig config = parseEndConfig(node.getConfigJson());
        String loopNodeId = config.getLoopNodeId();
        if (loopNodeId == null || loopNodeId.trim().isEmpty()) {
            throw new BusinessException("end_while 节点未配置关联的循环节点");
        }

        LoopState state = LoopState.from(context.getGlobal(LoopState.key(loopNodeId)));
        if (state == null) {
            throw new BusinessException("end_while 未找到关联的循环状态: " + loopNodeId);
        }

        // 读取 while 配置
        LoopConfig whileConfig = readWhileConfig(state);

        // 校验超时
        checkTimeout(state, context);

        // 聚合本次迭代结果
        if (!state.isCurrentIndexAggregated()) {
            Object aggregateValue = evaluateAggregate(config.getAggregateExpr(), context);
            state.addResult(aggregateValue);
            state.markCurrentIndexAggregated();
        }

        // 记录循环维度信息
        final Integer iterationIndex = state.getIterationCount();

        // 弹出当前迭代作用域
        context.popScope();
        context.popLoopFrame();

        // 迭代计数 +1
        state.nextIteration();

        // 判断是否继续
        if (state.getIterationCount() >= whileConfig.getMaxIterations()) {
            log.info("[流程实例:{}] while 循环达到最大迭代数，退出循环",
                    context.getInstanceId());
            return exitLoop(state, context, config);
        }

        if (!evaluateBoolean(whileConfig.getConditionExpr(), context)) {
            log.info("[流程实例:{}] while 条件为 false，退出循环",
                    context.getInstanceId());
            return exitLoop(state, context, config);
        }

        // 继续下一次迭代
        state.clearAggregatedFlag();
        context.setGlobal(state.getStateKey(), state.toMap());
        context.pushScope();

        return NodeExecuteResult.success()
                .withNextEntryNode(state.getBodyEntryNodeId(), loopNodeId, iterationIndex);
    }

    private LoopConfig readWhileConfig(LoopState state) {
        LoopConfig config = new LoopConfig();
        config.setLoopType("while");
        config.setConditionExpr(state.getConditionExpr());
        config.setMaxIterations(state.getMaxIterations());
        config.setTimeout(state.getTimeout());
        config.setResultVar(state.getResultVar());
        return config;
    }

    private Object evaluateAggregate(String aggregateExpr, FlowContext context) {
        if (aggregateExpr == null || aggregateExpr.trim().isEmpty()) {
            return null;
        }
        return LoopUtils.evaluateExpression(aggregateExpr, context);
    }

    private NodeExecuteResult exitLoop(LoopState state, FlowContext context, EndLoopConfig config) {
        context.setGlobal(state.getResultVar(), state.getResults());
        applyOutputMapping(config, context, state);
        context.removeGlobal(state.getStateKey());
        context.removeGlobal(state.getStartTimeKey());

        while (context.inScope()) {
            context.popScope();
        }
        while (context.getCurrentLoopFrame() != null) {
            context.popLoopFrame();
        }

        log.info("[流程实例:{}] while 循环退出，结果变量: {}, 结果数: {}",
                context.getInstanceId(), state.getResultVar(), state.getResults().size());

        return NodeExecuteResult.success().withExitLoop(true);
    }

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

    private void checkTimeout(LoopState state, FlowContext context) {
        Object startTimeObj = context.getGlobal(state.getStartTimeKey());
        if (startTimeObj == null) {
            return;
        }
        long startTime = Long.parseLong(startTimeObj.toString());
        long timeout = 30000L;
        Object loopStateObj = context.getGlobal(state.getStateKey());
        if (loopStateObj instanceof Map) {
            Object timeoutObj = ((Map<?, ?>) loopStateObj).get("timeout");
            if (timeoutObj != null) {
                timeout = Long.parseLong(timeoutObj.toString());
            }
        }
        if (System.currentTimeMillis() - startTime > timeout) {
            throw new BusinessException("while 循环执行超时");
        }
    }
}
