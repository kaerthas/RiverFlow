package com.riverflow.ai.dto;

import lombok.Data;

import java.util.List;

/**
 * AI 推荐数据映射响应
 */
@Data
public class AiGenerateMappingResponse {

    private List<MappingItem> mappings;
    private List<String> unmappedTargets;
    private List<String> unmappedSources;

    @Data
    public static class MappingItem {
        private String source;
        private String target;
        private String type;
        private Double confidence;
    }
}
