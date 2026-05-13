package com.riverflow.api.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.riverflow.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 流程边（连接线与条件）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wf_flow_edge")
public class FlowEdge extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 所属流程定义ID
     */
    private Long flowId;

    /**
     * 画布边ID
     */
    private String edgeId;

    /**
     * 源节点ID
     */
    private String sourceNode;

    /**
     * 目标节点ID
     */
    private String targetNode;

    /**
     * 条件类型：default/success/fail/custom
     */
    private String conditionType;

    /**
     * 自定义条件表达式（SpEL）
     */
    private String conditionExpression;

    /**
     * 优先级（数字小的先匹配）
     */
    private Integer priority;
}
