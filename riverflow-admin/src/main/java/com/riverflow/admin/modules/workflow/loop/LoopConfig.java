package com.riverflow.admin.modules.workflow.loop;

import lombok.Data;

/**
 * 循环节点配置基类
 */
@Data
public class LoopConfig {

    private String loopType;

    private String itemVar;

    private String indexVar;

    private String resultVar;

    private Integer maxIterations;

    private Integer timeout;

    private Boolean parallel;

    private Integer parallelLimit;

    private String breakExpr;

    private Boolean continueOnFail;

    private String emptyAction;

    // while 专用
    private String conditionExpr;

    // foreach 专用
    private String sourceExpr;
}
