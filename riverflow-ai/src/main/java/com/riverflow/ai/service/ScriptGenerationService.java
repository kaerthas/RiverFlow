package com.riverflow.ai.service;

import com.alibaba.fastjson2.JSON;
import com.riverflow.ai.audit.AiAuditLogService;
import com.riverflow.ai.client.AiChatClient;
import com.riverflow.ai.config.AiProperties;
import com.riverflow.ai.dto.AiGenerateScriptRequest;
import com.riverflow.ai.dto.AiGenerateScriptResponse;
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
 * Groovy 脚本智能生成服务
 */
@Slf4j
@Service
public class ScriptGenerationService {

    private static final String SCENE = "script";
    private static final String SYSTEM_PROMPT = "你是一个专业的 Groovy 脚本编写助手，擅长为政务流程编排生成安全、简洁的 Groovy 脚本。";

    private final AiChatClient aiChatClient;
    private final AiProperties aiProperties;
    private final AiAuditLogService auditLogService;
    private final PromptTemplateEngine templateEngine;
    private final PromptTemplateLoader templateLoader;
    private final AiResponseParser responseParser;
    private final AiOutputValidator outputValidator;
    private final AiOutputPostProcessor outputPostProcessor;

    @Autowired
    public ScriptGenerationService(AiChatClient aiChatClient, AiProperties aiProperties,
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
     * 生成 Groovy 脚本
     */
    public AiGenerateScriptResponse generate(AiGenerateScriptRequest request, String userId) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("userPrompt", request.getUserPrompt());
        variables.put("contextVariables", JSON.toJSONString(request.getContextVariables()));
        variables.put("expectedOutput", request.getExpectedOutput());

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

        AiGenerateScriptResponse result = responseParser.parseObject(response.getContent(), AiGenerateScriptResponse.class);
        result.setScriptContent(responseParser.sanitizeGroovy(result.getScriptContent()));
        outputPostProcessor.postProcess(result);
        outputValidator.validate(result);
        return result;
    }
}
