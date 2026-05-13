package com.riverflow.admin.modules.workflow.node;

import com.riverflow.admin.modules.workflow.context.FlowContext;
import com.riverflow.admin.modules.workflow.engine.NodeExecuteResult;
import com.riverflow.api.entity.FlowNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 结束节点执行器
 */
@Slf4j
@Component
public class EndNodeExecutor implements NodeExecutor {

    @Override
    public String getNodeType() {
        return "end";
    }

    @Override
    public NodeExecuteResult execute(FlowNode node, FlowContext context) {
        log.info("[流程实例:{}] 执行结束节点: {}", context.getInstanceId(), node.getNodeName());
        return NodeExecuteResult.success();
    }
}
