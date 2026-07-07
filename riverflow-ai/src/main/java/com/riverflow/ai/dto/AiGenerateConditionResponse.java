package com.riverflow.ai.dto;

import lombok.Data;

import java.util.List;

/**
 * AI 生成条件表达式响应
 */
@Data
public class AiGenerateConditionResponse {

    private String expression;
    private String explanation;
    private List<String> variables;
}
