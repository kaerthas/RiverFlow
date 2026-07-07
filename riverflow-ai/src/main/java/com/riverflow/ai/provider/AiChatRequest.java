package com.riverflow.ai.provider;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * LLM 聊天请求
 */
@Data
@Builder
public class AiChatRequest {

    /**
     * 模型名称，为空时使用 provider 默认模型
     */
    private String model;

    /**
     * 对话消息列表
     */
    @Builder.Default
    private List<AiMessage> messages = new ArrayList<>();

    /**
     * 温度，控制随机性
     */
    private Float temperature;

    /**
     * 最大生成 token 数
     */
    private Integer maxTokens;

    /**
     * 响应格式：json_object / text
     */
    private String responseFormat;

    /**
     * 功能场景，用于审计日志
     */
    private String scene;
}
