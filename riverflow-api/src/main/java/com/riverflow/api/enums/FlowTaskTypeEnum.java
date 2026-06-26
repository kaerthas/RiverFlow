package com.riverflow.api.enums;

/**
 * 流程任务类型
 */
public enum FlowTaskTypeEnum {
    /**
     * 普通节点任务
     */
    NODE("NODE", "节点任务"),

    /**
     * 并行循环的迭代子任务
     */
    LOOP_ITERATION("LOOP_ITERATION", "循环迭代任务"),

    /**
     * 并行循环的汇聚/聚合任务
     */
    LOOP_AGGREGATE("LOOP_AGGREGATE", "循环聚合任务");

    private final String code;
    private final String desc;

    FlowTaskTypeEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static FlowTaskTypeEnum fromCode(String code) {
        if (code == null) {
            return NODE;
        }
        for (FlowTaskTypeEnum value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return NODE;
    }
}
