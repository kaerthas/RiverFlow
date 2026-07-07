package com.riverflow.ai.service;

import com.alibaba.fastjson2.JSON;
import com.riverflow.ai.audit.AiAuditLogService;
import com.riverflow.ai.client.AiChatClient;
import com.riverflow.ai.config.AiProperties;
import com.riverflow.ai.dto.AiGenerateMappingRequest;
import com.riverflow.ai.dto.AiGenerateMappingResponse;
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
 * 智能数据映射推荐服务
 */
@Slf4j
@Service
public class MappingRecommendationService {

    private static final String SCENE = "mapping";
    private static final String SYSTEM_PROMPT = "你是一个专业的数据映射推荐助手，擅长根据 API 参数语义和流程上下文变量，自动推荐 input/output mapping 关系。";

    private final AiChatClient aiChatClient;
    private final AiProperties aiProperties;
    private final AiAuditLogService auditLogService;
    private final PromptTemplateEngine templateEngine;
    private final PromptTemplateLoader templateLoader;
    private final AiResponseParser responseParser;
    private final AiOutputValidator outputValidator;

    @Autowired
    public MappingRecommendationService(AiChatClient aiChatClient, AiProperties aiProperties,
                                        AiAuditLogService auditLogService, PromptTemplateEngine templateEngine,
                                        PromptTemplateLoader templateLoader, AiResponseParser responseParser,
                                        AiOutputValidator outputValidator) {
        this.aiChatClient = aiChatClient;
        this.aiProperties = aiProperties;
        this.auditLogService = auditLogService;
        this.templateEngine = templateEngine;
        this.templateLoader = templateLoader;
        this.responseParser = responseParser;
        this.outputValidator = outputValidator;
    }

    /**
     * 推荐数据映射
     */
    public AiGenerateMappingResponse recommend(AiGenerateMappingRequest request, String userId) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("userPrompt", request.getUserPrompt() != null ? request.getUserPrompt() : "");
        variables.put("direction", request.getDirection());
        variables.put("apiParams", JSON.toJSONString(request.getApiParams()));
        variables.put("contextVariables", JSON.toJSONString(request.getContextVariables()));
        variables.put("sampleResponse", JSON.toJSONString(request.getSampleResponse()));

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

        AiGenerateMappingResponse result = responseParser.parseObject(response.getContent(), AiGenerateMappingResponse.class);
        outputValidator.validate(result);
        return result;
    }
}
