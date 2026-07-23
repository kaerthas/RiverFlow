package com.riverflow.admin.modules.workflow.validate;

import com.riverflow.api.entity.FlowEdge;
import com.riverflow.api.entity.FlowNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 流程定义校验器
 *
 * <p>统一入口，组合多个校验规则对流程图做结构、语法、业务级校验。
 */
@Slf4j
@Component
public class FlowValidator {

    private final List<FlowValidationRule> rules;

    @Autowired(required = false)
    public FlowValidator(List<FlowValidationRule> rules) {
        this.rules = rules == null ? Collections.emptyList() : rules;
    }

    /**
     * 校验流程节点列表（兼容旧接口，默认无边）
     *
     * @param nodes 流程节点
     * @return 错误信息列表，空表示通过
     */
    public List<String> validate(List<FlowNode> nodes) {
        return validate(nodes, null).getErrors();
    }

    /**
     * 校验流程节点与边
     *
     * @param nodes 流程节点
     * @param edges 流程边
     * @return 校验结果
     */
    public FlowValidationResult validate(List<FlowNode> nodes, List<FlowEdge> edges) {
        FlowValidationResult result = new FlowValidationResult();
        result.setValid(true);

        if (rules.isEmpty()) {
            log.warn("当前未注册任何流程校验规则");
        }

        for (FlowValidationRule rule : rules) {
            try {
                if (rule == null) continue;
                FlowValidationResult subResult = rule.validate(nodes, edges);
                if (subResult != null) {
                    result.merge(subResult);
                }
            } catch (Exception e) {
                log.error("流程校验规则 [{}] 执行异常", rule.getClass().getSimpleName(), e);
                result.addError("校验规则 [" + rule.getClass().getSimpleName() + "] 执行异常: " + e.getMessage());
            }
        }

        return result;
    }

    /**
     * 快速判断是否通过校验
     */
    public boolean isValid(List<FlowNode> nodes, List<FlowEdge> edges) {
        return validate(nodes, edges).isValid();
    }
}
