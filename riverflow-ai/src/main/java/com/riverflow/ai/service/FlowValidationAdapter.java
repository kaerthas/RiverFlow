package com.riverflow.ai.service;

import com.riverflow.api.entity.FlowEdge;
import com.riverflow.api.entity.FlowNode;
import com.riverflow.api.modules.workflow.validate.FlowValidationResult;
import com.riverflow.api.modules.workflow.validate.FlowValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 流程校验适配器
 *
 * <p>将 AI 生成的流程草稿转换为 {@link FlowNode} / {@link FlowEdge} 后，
 * 复用下沉到 {@code riverflow-api} 的 {@link FlowValidator} 执行统一校验。
 */
@Component
public class FlowValidationAdapter {

    private final FlowValidator flowValidator;

    @Autowired
    public FlowValidationAdapter(FlowValidator flowValidator) {
        this.flowValidator = flowValidator;
    }

    /**
     * 执行流程校验
     *
     * @param nodes 流程节点
     * @param edges 流程边
     * @return 校验结果
     */
    public FlowValidationResult validate(List<FlowNode> nodes, List<FlowEdge> edges) {
        return flowValidator.validate(nodes, edges);
    }
}
