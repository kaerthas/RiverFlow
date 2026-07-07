package com.riverflow.ai.knowledge.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 流程定义（知识库来源）
 */
@Data
@TableName("wf_flow_definition")
public class FlowDefinition {

    private Long id;
    private String flowCode;
    private String flowName;
    private String triggerType;
    private String triggerConfig;
    private String executionMode;
    private String graphJson;
    private Integer status;
    private Integer delFlag;
}
