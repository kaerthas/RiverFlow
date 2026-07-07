package com.riverflow.ai.service;

import com.riverflow.ai.audit.AiAuditLogService;
import com.riverflow.ai.client.AiChatClient;
import com.riverflow.ai.config.AiProperties;
import com.riverflow.ai.provider.AiChatRequest;
import com.riverflow.ai.provider.AiChatResponse;
import com.riverflow.ai.provider.AiMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * AI 通用助手服务
 */
@Slf4j
@Service
public class AiAssistantService {

    private static final String SCENE = "chat";

    private final AiChatClient aiChatClient;
    private final AiProperties aiProperties;
    private final AiAuditLogService auditLogService;

    @Autowired
    public AiAssistantService(AiChatClient aiChatClient, AiProperties aiProperties,
                              AiAuditLogService auditLogService) {
        this.aiChatClient = aiChatClient;
        this.aiProperties = aiProperties;
        this.auditLogService = auditLogService;
    }

    /**
     * 通用对话
     */
    public String chat(String userMessage, String history, String provider, String model, String userId) {
        List<AiMessage> messages = new ArrayList<>();
        messages.add(AiMessage.system("你是 RiverFlow 流程编排平台的 AI 助手，帮助用户理解、设计、优化流程。"));
        if (history != null && !history.isBlank()) {
            messages.add(AiMessage.user(history));
        }
        messages.add(AiMessage.user(userMessage));

        AiChatRequest request = AiChatRequest.builder()
                .model(model)
                .messages(messages)
                .scene(SCENE)
                .build();

        AiChatResponse response;
        try {
            response = provider != null && !provider.isBlank()
                    ? aiChatClient.chat(provider, request, userId)
                    : aiChatClient.chat(request, userId);
        } catch (Exception e) {
            if (aiProperties.isAuditEnabled()) {
                auditLogService.logError(SCENE, userId, request, e.getMessage());
            }
            throw e;
        }

        if (aiProperties.isAuditEnabled()) {
            auditLogService.log(SCENE, userId, request, response);
        }
        return response.getContent();
    }
}
