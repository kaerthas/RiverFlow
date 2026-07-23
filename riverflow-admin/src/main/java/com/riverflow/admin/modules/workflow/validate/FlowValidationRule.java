package com.riverflow.admin.modules.workflow.validate;

import com.riverflow.api.entity.FlowEdge;
import com.riverflow.api.entity.FlowNode;

import java.util.List;

/**
 * 流程校验规则接口
 */
public interface FlowValidationRule {

    /**
     * 执行校验
     *
     * @param nodes 流程节点
     * @param edges 流程边
     * @return 校验结果
     */
    FlowValidationResult validate(List<FlowNode> nodes, List<FlowEdge> edges);
}
