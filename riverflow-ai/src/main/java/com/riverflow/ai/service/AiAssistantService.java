package com.riverflow.ai.service;

import com.riverflow.ai.audit.AiAuditLogService;
import com.riverflow.ai.client.AiChatClient;
import com.riverflow.ai.config.AiProperties;
import com.riverflow.ai.dto.AiChatReference;
import com.riverflow.ai.dto.AiChatResult;
import com.riverflow.ai.knowledge.service.KnowledgeRagService;
import com.riverflow.ai.knowledge.vector.VectorDocument;
import com.riverflow.ai.provider.AiChatRequest;
import com.riverflow.ai.provider.AiChatResponse;
import com.riverflow.ai.provider.AiMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * AI 通用助手服务
 */
@Slf4j
@Service
public class AiAssistantService {

    private static final String SCENE = "chat";
    private static final String SYSTEM_PROMPT = "你是 RiverFlow 流程编排平台的 AI 助手，帮助用户理解、设计、优化流程。";
    private static final int MAX_RAG_RESULTS = 5;

    private final AiChatClient aiChatClient;
    private final AiProperties aiProperties;
    private final AiAuditLogService auditLogService;
    private final KnowledgeRagService knowledgeRagService;

    @Autowired
    public AiAssistantService(AiChatClient aiChatClient, AiProperties aiProperties,
                              AiAuditLogService auditLogService,
                              KnowledgeRagService knowledgeRagService) {
        this.aiChatClient = aiChatClient;
        this.aiProperties = aiProperties;
        this.auditLogService = auditLogService;
        this.knowledgeRagService = knowledgeRagService;
    }

    /**
     * 通用对话（带 RAG 知识增强）
     */
    public AiChatResult chat(String userMessage, String history, Long collectionId, String provider, String model, String userId) {
        List<AiChatReference> references = retrieveReferences(userMessage, collectionId);
        List<AiMessage> messages = buildMessages(userMessage, history, references);

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

        AiChatResult result = new AiChatResult();
        result.setReply(response.getContent());
        result.setReferences(references);
        return result;
    }

    /**
     * 组装带 RAG 上下文的对话消息
     */
    public List<AiMessage> buildMessages(String userMessage, String history, List<AiChatReference> references) {
        List<AiMessage> messages = new ArrayList<>();
        StringBuilder systemPrompt = new StringBuilder(SYSTEM_PROMPT);
        if (!references.isEmpty()) {
            systemPrompt.append("\n\n【以下是与用户问题相关的平台知识，请优先参考】\n");
            for (int i = 0; i < references.size(); i++) {
                AiChatReference ref = references.get(i);
                systemPrompt.append("[").append(i + 1).append("] ")
                        .append("来源：").append(ref.getSourceType()).append("，")
                        .append("标题：").append(ref.getTitle()).append("\n")
                        .append(ref.getContent()).append("\n\n");
            }
        }
        messages.add(AiMessage.system(systemPrompt.toString()));
        if (StringUtils.hasText(history)) {
            messages.add(AiMessage.user(history));
        }
        messages.add(AiMessage.user(userMessage));
        return messages;
    }

    /**
     * 检索知识库引用
     */
    public List<AiChatReference> retrieveReferences(String userMessage, Long collectionId) {
        if (!aiProperties.getKnowledge().getRag().isEnabled()) {
            return Collections.emptyList();
        }
        if (!StringUtils.hasText(userMessage)) {
            return Collections.emptyList();
        }
        try {
            List<VectorDocument> docs = knowledgeRagService.search(userMessage, collectionId, null, MAX_RAG_RESULTS, null);
            List<AiChatReference> references = new ArrayList<>();
            for (VectorDocument doc : docs) {
                AiChatReference ref = new AiChatReference();
                ref.setSourceType(doc.getMetadata() != null ? String.valueOf(doc.getMetadata().get("sourceType")) : "unknown");
                ref.setTitle(doc.getMetadata() != null ? String.valueOf(doc.getMetadata().get("title")) : "");
                ref.setContent(doc.getContent());
                ref.setScore(doc.getScore());
                references.add(ref);
            }
            log.debug("AI 对话 RAG 检索完成: query={}, references={}", userMessage, references.size());
            return references;
        } catch (Exception e) {
            log.warn("AI 对话 RAG 检索失败，将不使用知识增强: query={}", userMessage, e);
            return Collections.emptyList();
        }
    }
}
