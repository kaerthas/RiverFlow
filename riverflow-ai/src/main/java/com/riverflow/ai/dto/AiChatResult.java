package com.riverflow.ai.dto;

import lombok.Data;

import java.util.List;

/**
 * AI 通用对话响应（带知识库引用）
 */
@Data
public class AiChatResult {

    /**
     * AI 回复内容
     */
    private String reply;

    /**
     * 引用的知识库片段
     */
    private List<AiChatReference> references;
}
