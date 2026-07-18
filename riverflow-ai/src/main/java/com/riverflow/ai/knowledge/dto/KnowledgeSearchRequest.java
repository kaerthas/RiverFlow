package com.riverflow.ai.knowledge.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 语义检索请求
 */
@Data
public class KnowledgeSearchRequest {

    @NotBlank(message = "查询文本不能为空")
    private String query;

    /**
     * 指定集合
     */
    private String collection;

    /**
     * Top-K
     */
    private Integer topK;

    /**
     * 最低相似度
     */
    private Double minScore;
}
