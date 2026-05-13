package com.riverflow.api.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.riverflow.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 流程定义
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wf_flow_definition")
public class FlowDefinition extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 流程编码
     */
    private String flowCode;

    /**
     * 流程名称
     */
    private String flowName;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 绑定的事项编码
     */
    private String itemCode;

    /**
     * 触发方式：cron-定时 event-事件 manual-手动
     */
    private String triggerType;

    /**
     * 触发配置（cron表达式或事件类型）
     */
    private String triggerConfig;

    /**
     * 状态：0-草稿，1-已发布，2-下线
     */
    private Integer status;

    /**
     * 流程图JSON（LogicFlow格式）
     */
    private String graphJson;
}
