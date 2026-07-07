package com.riverflow.ai.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * AI 调用统计看板数据
 */
@Data
public class AiCallStats implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 总调用次数 */
    private Long totalCalls = 0L;

    /** 成功次数 */
    private Long successCalls = 0L;

    /** 失败次数 */
    private Long failCalls = 0L;

    /** 总 Token 数 */
    private Long totalTokens = 0L;

    /** 平均响应时间(ms) */
    private Double avgResponseTime = 0.0;

    /** 按场景统计 */
    private List<NameValue> byScene = new ArrayList<>();

    /** 按日期统计 */
    private List<NameValue> byDate = new ArrayList<>();

    /** 按 Provider 统计 */
    private List<NameValue> byProvider = new ArrayList<>();

    @Data
    public static class NameValue implements Serializable {
        private static final long serialVersionUID = 1L;
        private String name;
        private Long value;

        public NameValue() {
        }

        public NameValue(String name, Long value) {
            this.name = name;
            this.value = value;
        }
    }
}
