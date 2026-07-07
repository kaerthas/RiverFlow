package com.riverflow.ai.service;

import com.alibaba.fastjson2.JSON;
import com.riverflow.ai.audit.AiAuditLogService;
import com.riverflow.ai.client.AiChatClient;
import com.riverflow.ai.config.AiProperties;
import com.riverflow.ai.dto.AiGenerateConditionRequest;
import com.riverflow.ai.dto.AiGenerateConditionResponse;
import com.riverflow.ai.parser.AiOutputPostProcessor;
import com.riverflow.ai.parser.AiOutputValidator;
import com.riverflow.ai.parser.AiResponseParser;
import com.riverflow.ai.prompt.PromptTemplateEngine;
import com.riverflow.ai.prompt.PromptTemplateLoader;
import com.riverflow.ai.provider.AiChatRequest;
import com.riverflow.ai.provider.AiChatResponse;
import com.riverflow.ai.provider.AiMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 智能条件表达式生成服务
 */
@Slf4j
@Service
public class ConditionGenerationService {

    private static final String SCENE = "condition";
    private static final String SYSTEM_PROMPT = "你是一个专业的政务流程编排助手，擅长将自然语言需求转换为 SpEL 条件表达式。";

    private final AiChatClient aiChatClient;
    private final AiProperties aiProperties;
    private final AiAuditLogService auditLogService;
    private final PromptTemplateEngine templateEngine;
    private final PromptTemplateLoader templateLoader;
    private final AiResponseParser responseParser;
    private final AiOutputValidator outputValidator;
    private final AiOutputPostProcessor outputPostProcessor;

    @Autowired
    public ConditionGenerationService(AiChatClient aiChatClient, AiProperties aiProperties,
                                      AiAuditLogService auditLogService, PromptTemplateEngine templateEngine,
                                      PromptTemplateLoader templateLoader, AiResponseParser responseParser,
                                      AiOutputValidator outputValidator, AiOutputPostProcessor outputPostProcessor) {
        this.aiChatClient = aiChatClient;
        this.aiProperties = aiProperties;
        this.auditLogService = auditLogService;
        this.templateEngine = templateEngine;
        this.templateLoader = templateLoader;
        this.responseParser = responseParser;
        this.outputValidator = outputValidator;
        this.outputPostProcessor = outputPostProcessor;
    }

    /**
     * 生成 SpEL 条件表达式
     */
    public AiGenerateConditionResponse generate(AiGenerateConditionRequest request, String userId) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("userPrompt", request.getUserPrompt());
        variables.put("contextVariables", JSON.toJSONString(request.getContextVariables()));

        String template = templateLoader.load(SCENE);
        String userPrompt = templateEngine.render(template, variables);

        String provider = request.getProvider();

        AiChatRequest chatRequest = AiChatRequest.builder()
                .model(request.getModel())
                .messages(List.of(AiMessage.system(SYSTEM_PROMPT), AiMessage.user(userPrompt)))
                .responseFormat("json_object")
                .scene(SCENE)
                .build();

        AiChatResponse response;
        try {
            response = provider != null && !provider.isBlank()
                    ? aiChatClient.chat(provider, chatRequest, userId)
                    : aiChatClient.chat(chatRequest, userId);
        } catch (Exception e) {
            if (aiProperties.isAuditEnabled()) {
                auditLogService.logError(SCENE, userId, chatRequest, e.getMessage());
            }
            throw e;
        }
        if (aiProperties.isAuditEnabled()) {
            auditLogService.log(SCENE, userId, chatRequest, response);
        }

        AiGenerateConditionResponse result = responseParser.parseObject(response.getContent(), AiGenerateConditionResponse.class);
        result.setExpression(responseParser.sanitizeSpel(result.getExpression()));
        outputPostProcessor.postProcess(result);
        outputValidator.validate(result);
        return result;
    }
}
