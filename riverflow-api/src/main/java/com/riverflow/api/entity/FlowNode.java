package com.riverflow.api.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.riverflow.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 流程节点
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wf_flow_node")
public class FlowNode extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 所属流程定义ID
     */
    private Long flowId;

    /**
     * 画布节点ID（前端生成的唯一标识）
     */
    private String nodeId;

    /**
     * 节点名称
     */
    private String nodeName;

    /**
     * 节点类型：start/api/db/script/condition/timer/end
     */
    private String nodeType;

    /**
     * 节点配置JSON
     */
    private String configJson;

    /**
     * 输入映射JSON
     */
    private String inputMapping;

    /**
     * 输出映射JSON
     */
    private String outputMapping;

    /**
     * Cron表达式（仅timer节点）
     */
    private String cronExpression;

    /**
     * 超时毫秒
     */
    private Integer timeout;

    /**
     * 重试次数
     */
    private Integer retryTimes;

    /**
     * 失败策略：suspend-挂起 skip-跳过 retry-重试
     */
    private String failStrategy;

    /**
     * 排序号
     */
    private Integer sortNo;

    /**
     * X坐标
     */
    private BigDecimal xCoordinate;

    /**
     * Y坐标
     */
    private BigDecimal yCoordinate;
}
