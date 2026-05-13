package com.riverflow.admin.modules.workflow.node;

import com.riverflow.admin.modules.workflow.context.FlowContext;
import com.riverflow.admin.modules.workflow.engine.NodeExecuteResult;
import com.riverflow.api.entity.FlowNode;

/**
 * 节点执行器接口
 * 每种节点类型对应一个实现类
 */
public interface NodeExecutor {

    /**
     * 获取支持的节点类型
     */
    String getNodeType();

    /**
     * 执行节点逻辑
     *
     * @param node    节点定义
     * @param context 流程上下文
     * @return 执行结果
     */
    NodeExecuteResult execute(FlowNode node, FlowContext context);
}
