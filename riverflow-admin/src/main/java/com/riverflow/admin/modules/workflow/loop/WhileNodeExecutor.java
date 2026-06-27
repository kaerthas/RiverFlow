package com.riverflow.admin.modules.workflow.loop;

import com.riverflow.admin.modules.workflow.context.FlowContext;
import com.riverflow.admin.modules.workflow.engine.NodeExecuteResult;
import com.riverflow.admin.modules.workflow.node.NodeExecutor;
import com.riverflow.api.entity.FlowNode;
import com.riverflow.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.riverflow.admin.modules.workflow.loop.LoopUtils.parseConfig;
import static com.riverflow.admin.modules.workflow.loop.LoopUtils.resolveBodyEntryNodeId;
import static com.riverflow.admin.modules.workflow.loop.LoopUtils.resolveEndLoopNodeId;
import static com.riverflow.admin.modules.workflow.loop.LoopUtils.evaluateBoolean;

/**
 * while 循环开始节点执行器
 */
@Slf4j
@Component
public class WhileNodeExecutor implements NodeExecutor {

    @Override
    public String getNodeType() {
        return "while";
    }

    @Override
    public NodeExecuteResult execute(FlowNode node, FlowContext context) {
        log.info("[流程实例:{}] 执行 while 节点: {}", context.getInstanceId(), node.getNodeName());

        // 进入新一次循环迭代前清理表达式缓存，避免循环体中 evaluateCollection 使用旧值
        context.clearEvaluationCache();

        LoopConfig config = parseConfig(node.getConfigJson());
        if (config.getConditionExpr() == null || config.getConditionExpr().trim().isEmpty()) {
            throw new BusinessException("while 节点缺少条件表达式");
        }

        // 检查现有状态
        LoopState state = LoopState.from(context.getGlobal(LoopState.key(node.getNodeId())));

        if (state == null) {
            // 首次进入 while
            if (!evaluateBoolean(config.getConditionExpr(), context)) {
                log.info("[流程实例:{}] while 条件为 false，跳过循环",
                        context.getInstanceId());
                return NodeExecuteResult.success().withExitLoop(true);
            }

            state = new LoopState(node.getNodeId(), config);
            state.setBodyEntryNodeId(resolveBodyEntryNodeId(node.getNodeId(), context.getNodes(), context.getEdges()));
            state.setEndNodeId(resolveEndLoopNodeId(node.getNodeId(), node.getNodeType(), context.getNodes(), context.getEdges()));
            state.setInitialized(true);
            state.setStartTime(System.currentTimeMillis());

            context.setGlobal(state.getStateKey(), state.toMap());
            context.setGlobal(state.getStartTimeKey(), state.getStartTime());
        } else {
            // 幂等：已初始化则恢复
            if (state.getBodyEntryNodeId() == null) {
                state.setBodyEntryNodeId(resolveBodyEntryNodeId(node.getNodeId(), context.getNodes(), context.getEdges()));
            }
        }

        // 压入作用域
        context.pushScope();

        log.info("[流程实例:{}] while 节点进入第 {} 次迭代，入口节点: {}",
                context.getInstanceId(), state.getIterationCount() + 1, state.getBodyEntryNodeId());

        return NodeExecuteResult.success()
                .withNextEntryNode(state.getBodyEntryNodeId(), state.getLoopNodeId(), state.getIterationCount());
    }
}
