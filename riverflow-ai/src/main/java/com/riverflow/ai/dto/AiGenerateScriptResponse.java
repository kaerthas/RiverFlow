package com.riverflow.ai.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * AI 生成 Groovy 脚本响应
 */
@Data
public class AiGenerateScriptResponse {

    private String scriptContent;
    private String explanation;
    private List<Map<String, String>> outputMapping;
}
