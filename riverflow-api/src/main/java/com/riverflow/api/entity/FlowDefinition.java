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
     * 部门ID（数据权限）
     */
    private Long deptId;

    /**
     * 触发方式：cron-定时 event-事件 manual-手动
     */
    private String triggerType;

    /**
     * 触发配置（cron表达式或事件类型）
     */
    private String triggerConfig;

    /**
     * 状态：0-草稿，1-已发布，2-下线，3-待复核
     */
    private Integer status;

    /**
     * 来源：ai-AI生成 manual-手动创建
     */
    private String source;

    /**
     * 复核备注（复核拒绝时填写）
     */
    private String reviewRemark;

    /**
     * 执行模式：ASYNC-异步(默认) SYNC-同步
     */
    private String executionMode;

    /**
     * 同步流程默认入参(JSON)，启动时自动注入上下文
     */
    private String inputParams;

    /**
     * 同步流程输出参数(JSON)，用于声明流程返回结果结构
     */
    private String outputParams;

    /**
     * 流程图JSON（LogicFlow格式）
     */
    private String graphJson;
}
