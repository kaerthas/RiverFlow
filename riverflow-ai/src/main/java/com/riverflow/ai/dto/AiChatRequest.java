package com.riverflow.ai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * AI 通用对话请求
 */
@Data
public class AiChatRequest {

    /**
     * 用户输入消息
     */
    @NotBlank(message = "消息内容不能为空")
    private String message;

    /**
     * 指定 provider，为空时使用默认 provider
     */
    private String provider;

    /**
     * 指定模型，为空时使用 provider 默认模型
     */
    private String model;

    /**
     * 历史对话上下文
     */
    private String history;
}
