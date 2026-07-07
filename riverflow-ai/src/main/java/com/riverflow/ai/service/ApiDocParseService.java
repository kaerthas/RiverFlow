package com.riverflow.ai.service;

import com.alibaba.fastjson2.JSON;
import com.riverflow.ai.audit.AiAuditLogService;
import com.riverflow.ai.client.AiChatClient;
import com.riverflow.ai.config.AiProperties;
import com.riverflow.ai.dto.AiParseApiDocRequest;
import com.riverflow.ai.dto.AiParseApiDocResponse;
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
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 接口文档智能解析服务
 *
 * <p>从 OpenAPI/Swagger JSON 或接口描述中提取 API 元数据，为后续自动注册提供数据。
 */
@Slf4j
@Service
public class ApiDocParseService {

    private static final String SCENE = "api-doc-parse";
    private static final String SYSTEM_PROMPT = "你是一名资深的 API 文档解析专家，擅长从 OpenAPI 3.0 / Swagger 2.0 JSON 或自然语言接口文档中提取结构化的 API 元数据。";

    private final AiChatClient aiChatClient;
    private final AiProperties aiProperties;
    private final AiAuditLogService auditLogService;
    private final PromptTemplateEngine templateEngine;
    private final PromptTemplateLoader templateLoader;
    private final AiResponseParser responseParser;
    private final AiOutputValidator outputValidator;

    @Autowired
    public ApiDocParseService(AiChatClient aiChatClient, AiProperties aiProperties,
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
     * 解析接口文档
     */
    public AiParseApiDocResponse parse(AiParseApiDocRequest request, String userId) {
        List<String> options = request.getOptions();
        if (CollectionUtils.isEmpty(options)) {
            options = List.of("extractParams", "extractResponses");
        }

        Map<String, Object> variables = new HashMap<>();
        variables.put("docContent", request.getDocContent());
        variables.put("options", JSON.toJSONString(options));

        String template = templateLoader.load(SCENE);
        String userPrompt = templateEngine.render(template, variables);

        String provider = request.getProvider();

        AiChatRequest chatRequest = AiChatRequest.builder()
                .model(request.getModel())
                .messages(List.of(AiMessage.system(SYSTEM_PROMPT), AiMessage.user(userPrompt)))
                .responseFormat("json_object")
                .scene(SCENE)
                .maxTokens(8192)
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

        String json = responseParser.extractJson(response.getContent());
        json = normalizeApiDocJson(json);
        AiParseApiDocResponse result = JSON.parseObject(json, AiParseApiDocResponse.class);
        outputValidator.validate(result);
        return result;
    }

    /**
     * 归一化模型返回的接口文档解析 JSON，修复常见类型错误
     */
    private String normalizeApiDocJson(String json) {
        if (json == null || json.isBlank()) {
            return json;
        }
        // requestBody 应为对象，模型可能错写成空数组
        json = json.replaceAll("\"requestBody\"\\s*:\\s*\\[\\s*]", "\"requestBody\":{\"contentType\":\"\",\"schemaJson\":\"{}\",\"fields\":[]}");
        return json;
    }
}
