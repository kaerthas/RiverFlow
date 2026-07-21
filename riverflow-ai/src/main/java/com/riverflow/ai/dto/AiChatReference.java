package com.riverflow.ai.dto;

import lombok.Data;

/**
 * AI 对话引用的知识库片段
 */
@Data
public class AiChatReference {

    /**
     * 来源类型：flow / api / datasource / upload 等
     */
    private String sourceType;

    /**
     * 来源标题
     */
    private String title;

    /**
     * 引用内容
     */
    private String content;

    /**
     * 相似度分数
     */
    private Double score;
}
