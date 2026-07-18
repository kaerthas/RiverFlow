package com.riverflow.ai.service;

import com.alibaba.fastjson2.JSON;
import com.riverflow.ai.audit.AiAuditLogService;
import com.riverflow.ai.client.AiChatClient;
import com.riverflow.ai.config.AiProperties;
import com.riverflow.ai.dto.AiGenerateConditionRequest;
import com.riverflow.ai.dto.AiGenerateConditionResponse;
import com.riverflow.ai.parser.AiJsonSchemaValidator;
import com.riverflow.ai.parser.AiOutputPostProcessor;
import com.riverflow.ai.parser.AiOutputValidator;
import com.riverflow.ai.parser.AiResponseParser;
import com.riverflow.ai.prompt.PromptTemplateEngine;
import com.riverflow.ai.prompt.PromptTemplateLoader;
import com.riverflow.ai.prompt.dto.PromptContent;
import com.riverflow.ai.provider.AiChatRequest;
import com.riverflow.ai.provider.AiChatResponse;
import com.riverflow.ai.provider.AiMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
    private static final String DEFAULT_SYSTEM_PROMPT = "你是一个专业的政务流程编排助手，擅长将自然语言需求转换为 SpEL 条件表达式。";

    private final AiChatClient aiChatClient;
    private final AiProperties aiProperties;
    private final AiAuditLogService auditLogService;
    private final PromptTemplateEngine templateEngine;
    private final PromptTemplateLoader templateLoader;
    private final AiResponseParser responseParser;
    private final AiOutputValidator outputValidator;
    private final AiOutputPostProcessor outputPostProcessor;
    private final AiJsonSchemaValidator schemaValidator;

    @Autowired
    public ConditionGenerationService(AiChatClient aiChatClient, AiProperties aiProperties,
                                      AiAuditLogService auditLogService, PromptTemplateEngine templateEngine,
                                      PromptTemplateLoader templateLoader, AiResponseParser responseParser,
                                      AiOutputValidator outputValidator, AiOutputPostProcessor outputPostProcessor,
                                      AiJsonSchemaValidator schemaValidator) {
        this.aiChatClient = aiChatClient;
        this.aiProperties = aiProperties;
        this.auditLogService = auditLogService;
        this.templateEngine = templateEngine;
        this.templateLoader = templateLoader;
        this.responseParser = responseParser;
        this.outputValidator = outputValidator;
        this.outputPostProcessor = outputPostProcessor;
        this.schemaValidator = schemaValidator;
    }

    /**
     * 生成 SpEL 条件表达式
     */
    public AiGenerateConditionResponse generate(AiGenerateConditionRequest request, String userId) {
        String model = resolveModel(request.getModel());
        PromptContent promptContent = templateLoader.load(SCENE, model, request.getPromptVersion());
        Map<String, Object> variables = buildPromptVariables(request, promptContent);

        String userPrompt = templateEngine.render(promptContent.getTemplate(), variables);
        String systemPrompt = StringUtils.hasText(promptContent.getSystemPrompt())
                ? promptContent.getSystemPrompt() : DEFAULT_SYSTEM_PROMPT;

        String provider = request.getProvider();

        AiChatRequest chatRequest = AiChatRequest.builder()
                .model(request.getModel())
                .messages(List.of(AiMessage.system(systemPrompt), AiMessage.user(userPrompt)))
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
                auditLogService.logError(SCENE, userId, chatRequest, e.getMessage(), buildPromptVersion(promptContent));
            }
            throw e;
        }
        if (aiProperties.isAuditEnabled()) {
            auditLogService.log(SCENE, userId, chatRequest, response, buildPromptVersion(promptContent));
        }

        validateSchema(response.getContent(), promptContent.getOutputSchema());
        AiGenerateConditionResponse result = responseParser.parseObject(response.getContent(), AiGenerateConditionResponse.class);
        result.setExpression(responseParser.sanitizeSpel(result.getExpression()));
        outputPostProcessor.postProcess(result);
        outputValidator.validate(result);
        return result;
    }

    private Map<String, Object> buildPromptVariables(AiGenerateConditionRequest request, PromptContent promptContent) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("userPrompt", request.getUserPrompt());
        variables.put("contextVariables", JSON.toJSONString(request.getContextVariables()));
        variables.put("extraContext", JSON.toJSONString(request.getExtraContext()));
        variables.put("outputSchema", StringUtils.hasText(promptContent.getOutputSchema())
                ? promptContent.getOutputSchema() : "");
        variables.put("examples", StringUtils.hasText(promptContent.getExamples())
                ? promptContent.getExamples() : "[]");
        return variables;
    }

    private String resolveModel(String model) {
        return StringUtils.hasText(model) ? model : "default";
    }

    private String buildPromptVersion(PromptContent promptContent) {
        return promptContent.getScene() + ":" + promptContent.getModel() + ":" + promptContent.getVersion();
    }

    private void validateSchema(String content, String outputSchema) {
        if (!StringUtils.hasText(outputSchema)) {
            return;
        }
        String json = responseParser.extractJson(content);
        schemaValidator.validate(json, outputSchema);
    }
}
