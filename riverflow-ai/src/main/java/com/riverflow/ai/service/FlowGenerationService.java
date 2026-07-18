package com.riverflow.ai.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.riverflow.ai.audit.AiAuditLogService;
import com.riverflow.ai.client.AiChatClient;
import com.riverflow.ai.config.AiProperties;
import com.riverflow.ai.dto.AiGenerateFlowRequest;
import com.riverflow.ai.dto.AiGenerateFlowResponse;
import com.riverflow.ai.knowledge.entity.ApiCatalog;
import com.riverflow.ai.knowledge.entity.Datasource;
import com.riverflow.ai.knowledge.entity.FlowDefinition;
import com.riverflow.ai.knowledge.service.AiKnowledgeService;
import com.riverflow.ai.parser.AiJsonSchemaValidator;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * AI 智能流程生成服务
 */
@Slf4j
@Service
public class FlowGenerationService {

    private static final String SCENE = "flow-generation";
    private static final String DEFAULT_SYSTEM_PROMPT = "你是一个专业的政务流程编排专家，擅长将自然语言业务需求转换为可视化的 RiverFlow 流程定义。";

    private final AiChatClient aiChatClient;
    private final AiProperties aiProperties;
    private final AiAuditLogService auditLogService;
    private final PromptTemplateEngine templateEngine;
    private final PromptTemplateLoader templateLoader;
    private final AiResponseParser responseParser;
    private final AiOutputValidator outputValidator;
    private final AiJsonSchemaValidator schemaValidator;
    private final AiKnowledgeService knowledgeService;

    @Autowired
    public FlowGenerationService(AiChatClient aiChatClient, AiProperties aiProperties,
                                 AiAuditLogService auditLogService, PromptTemplateEngine templateEngine,
                                 PromptTemplateLoader templateLoader, AiResponseParser responseParser,
                                 AiOutputValidator outputValidator, AiJsonSchemaValidator schemaValidator,
                                 AiKnowledgeService knowledgeService) {
        this.aiChatClient = aiChatClient;
        this.aiProperties = aiProperties;
        this.auditLogService = auditLogService;
        this.templateEngine = templateEngine;
        this.templateLoader = templateLoader;
        this.responseParser = responseParser;
        this.outputValidator = outputValidator;
        this.schemaValidator = schemaValidator;
        this.knowledgeService = knowledgeService;
    }

    /**
     * 自然语言生成流程草稿
     */
    public AiGenerateFlowResponse generate(AiGenerateFlowRequest request, String userId) {
        String model = resolveModel(request.getModel());
        PromptContent promptContent = templateLoader.load(SCENE, model, request.getPromptVersion());
        Map<String, Object> variables = buildPromptVariables(request, promptContent);

        String userPrompt = templateEngine.render(promptContent.getTemplate(), variables);
        String systemPrompt = StringUtils.hasText(promptContent.getSystemPrompt())
                ? promptContent.getSystemPrompt() : DEFAULT_SYSTEM_PROMPT;

        String provider = request.getProvider();
        log.info("开始 AI 流程生成, userId={}, provider={}, model={}", userId, provider, request.getModel());
        long start = System.currentTimeMillis();

        AiChatRequest chatRequest = AiChatRequest.builder()
                .model(request.getModel())
                .messages(List.of(AiMessage.system(systemPrompt), AiMessage.user(userPrompt)))
                .responseFormat("json_object")
                .scene(SCENE)
                .maxTokens(16384)
                .build();

        AiChatResponse response;
        try {
            response = provider != null && !provider.isBlank()
                    ? aiChatClient.chat(provider, chatRequest, userId)
                    : aiChatClient.chat(chatRequest, userId);
            log.info("AI 流程生成调用完成, userId={}, cost={}ms, promptTokens={}, completionTokens={}",
                    userId, System.currentTimeMillis() - start,
                    response.getPromptTokens(), response.getCompletionTokens());
        } catch (Exception e) {
            log.error("AI 流程生成调用失败, userId={}, cost={}ms", userId, System.currentTimeMillis() - start, e);
            if (aiProperties.isAuditEnabled()) {
                auditLogService.logError(SCENE, userId, chatRequest, e.getMessage(), buildPromptVersion(promptContent));
            }
            throw e;
        }
        if (aiProperties.isAuditEnabled()) {
            auditLogService.log(SCENE, userId, chatRequest, response, buildPromptVersion(promptContent));
        }

        String thinking = responseParser.extractThink(response.getContent());
        validateSchema(response.getContent(), promptContent.getOutputSchema());
        AiGenerateFlowResponse result = responseParser.parseObject(response.getContent(), AiGenerateFlowResponse.class);
        result.setThinking(thinking);
        syncNodesEdgesFromGraphJson(result);
        outputValidator.validate(result);
        return result;
    }

    /**
     * 自然语言生成流程草稿（流式输出）
     *
     * <p>流式返回思考过程和最终 JSON 结果。
     * 事件格式：
     * - data: [THINK]思考内容片段
     * - data: [JSON]{"flowName":"..."}
     * - data: [ERROR]错误信息
     * - data: [DONE]
     */
    public void generateStream(AiGenerateFlowRequest request, String userId,
                               Consumer<String> onThink,
                               Consumer<AiGenerateFlowResponse> onResult,
                               Consumer<Throwable> onError,
                               Runnable onComplete) {
        String model = resolveModel(request.getModel());
        PromptContent promptContent = templateLoader.load(SCENE, model, request.getPromptVersion());
        Map<String, Object> variables = buildPromptVariables(request, promptContent);

        String prompt = templateEngine.render(promptContent.getTemplate(), variables);
        String systemPrompt = StringUtils.hasText(promptContent.getSystemPrompt())
                ? promptContent.getSystemPrompt() : DEFAULT_SYSTEM_PROMPT;

        String provider = request.getProvider();
        log.info("开始 AI 流程生成流式调用, userId={}, provider={}, model={}", userId, provider, request.getModel());

        AiChatRequest chatRequest = AiChatRequest.builder()
                .model(request.getModel())
                .messages(List.of(AiMessage.system(systemPrompt), AiMessage.user(prompt)))
                .responseFormat("json_object")
                .scene(SCENE)
                .maxTokens(16384)
                .build();

        StringBuilder buffer = new StringBuilder();
        AtomicInteger thinkStart = new AtomicInteger(-1);
        AtomicInteger thinkEnd = new AtomicInteger(-1);
        AtomicInteger sentThinkLen = new AtomicInteger(0);
        AtomicBoolean jsonStarted = new AtomicBoolean(false);

        Consumer<String> dataHandler = chunk -> {
            buffer.append(chunk);
            String current = buffer.toString();

            if (thinkStart.get() < 0) {
                int start = current.indexOf("<think>");
                if (start >= 0) {
                    thinkStart.set(start + 7);
                }
            }

            if (thinkStart.get() >= 0 && thinkEnd.get() < 0) {
                int end = current.indexOf("</think>", thinkStart.get());
                if (end >= 0) {
                    thinkEnd.set(end);
                    String think = current.substring(thinkStart.get(), thinkEnd.get());
                    if (think.length() > sentThinkLen.get()) {
                        onThink.accept(think.substring(sentThinkLen.get()));
                        sentThinkLen.set(think.length());
                    }
                } else {
                    String think = current.substring(thinkStart.get());
                    // </think> 可能被 chunk 切分，末尾若出现其前缀则先发到前缀之前
                    int ltIndex = current.lastIndexOf('<');
                    if (ltIndex >= thinkStart.get() && current.length() - ltIndex < 8 && "</think>".startsWith(current.substring(ltIndex))) {
                        think = current.substring(thinkStart.get(), ltIndex);
                    }
                    if (think.length() > sentThinkLen.get()) {
                        onThink.accept(think.substring(sentThinkLen.get()));
                        sentThinkLen.set(think.length());
                    }
                }
            } else if (thinkStart.get() < 0 && !jsonStarted.get()) {
                // 模型未使用 <think> 标签时，把 JSON 开始前的内容作为思考过程
                int braceStart = current.indexOf('{');
                if (braceStart >= 0) {
                    jsonStarted.set(true);
                    if (braceStart > sentThinkLen.get()) {
                        onThink.accept(current.substring(sentThinkLen.get(), braceStart));
                        sentThinkLen.set(braceStart);
                    }
                } else {
                    if (current.length() > sentThinkLen.get()) {
                        onThink.accept(current.substring(sentThinkLen.get()));
                        sentThinkLen.set(current.length());
                    }
                }
            }
        };

        PromptContent finalPromptContent = promptContent;
        Runnable completionHandler = () -> {
            try {
                String content = buffer.toString();
                validateSchema(content, finalPromptContent.getOutputSchema());
                String thinking = responseParser.extractThink(content);
                AiGenerateFlowResponse result = responseParser.parseObject(content, AiGenerateFlowResponse.class);
                result.setThinking(thinking);
                syncNodesEdgesFromGraphJson(result);
                outputValidator.validate(result);
                onResult.accept(result);
                onComplete.run();
            } catch (Exception e) {
                log.error("AI 流程生成流式结果解析失败", e);
                onError.accept(e);
            }
        };

        try {
            if (provider != null && !provider.isBlank()) {
                aiChatClient.stream(provider, chatRequest, dataHandler, onError, completionHandler);
            } else {
                aiChatClient.stream(chatRequest, dataHandler, onError, completionHandler);
            }
        } catch (Exception e) {
            log.error("AI 流程生成流式调用失败", e);
            onError.accept(e);
        }
    }

    /**
     * 组装 Prompt 变量，并检索知识库补充相关接口/流程/数据源
     */
    private Map<String, Object> buildPromptVariables(AiGenerateFlowRequest request, PromptContent promptContent) {
        Map<String, Object> variables = new HashMap<>();
        String userPrompt = request.getUserPrompt();
        variables.put("userPrompt", userPrompt);
        variables.put("availableApis", JSON.toJSONString(request.getAvailableApis()));
        variables.put("availableDbSources", JSON.toJSONString(request.getAvailableDbSources()));
        variables.put("extraContext", JSON.toJSONString(request.getExtraContext()));
        variables.put("outputSchema", StringUtils.hasText(promptContent.getOutputSchema())
                ? promptContent.getOutputSchema() : "");
        variables.put("examples", StringUtils.hasText(promptContent.getExamples())
                ? promptContent.getExamples() : "[]");

        try {
            buildRelatedKnowledge(userPrompt, variables);
        } catch (Exception e) {
            log.warn("AI 知识库检索失败，将不使用相关知识", e);
            variables.put("relatedApis", "[]");
            variables.put("relatedFlows", "[]");
            variables.put("relatedDbSources", "[]");
        }
        return variables;
    }

    private void buildRelatedKnowledge(String userPrompt, Map<String, Object> variables) {
        // 优先使用语义检索
        Map<String, List<com.riverflow.ai.knowledge.vector.VectorDocument>> semanticResults =
                knowledgeService.searchSemanticGrouped(userPrompt, null, null, null);

        List<JSONObject> relatedApis = new ArrayList<>();
        List<JSONObject> relatedFlows = new ArrayList<>();
        List<JSONObject> relatedDbSources = new ArrayList<>();

        if (semanticResults != null && !semanticResults.isEmpty()) {
            for (Map.Entry<String, List<com.riverflow.ai.knowledge.vector.VectorDocument>> entry : semanticResults.entrySet()) {
                String sourceType = entry.getKey();
                List<com.riverflow.ai.knowledge.vector.VectorDocument> docs = entry.getValue();
                for (com.riverflow.ai.knowledge.vector.VectorDocument doc : docs) {
                    JSONObject obj = new JSONObject();
                    obj.put("title", doc.getMetadata() != null ? doc.getMetadata().get("title") : "");
                    obj.put("content", doc.getContent());
                    obj.put("score", doc.getScore());
                    obj.put("sourceType", sourceType);
                    switch (sourceType) {
                        case "api" -> relatedApis.add(obj);
                        case "flow" -> relatedFlows.add(obj);
                        case "datasource" -> relatedDbSources.add(obj);
                        default -> relatedFlows.add(obj);
                    }
                }
            }
        }

        // 语义检索为空时，使用 MySQL LIKE 兜底
        if (relatedApis.isEmpty()) {
            List<ApiCatalog> apis = knowledgeService.searchApis(userPrompt, 5);
            for (ApiCatalog api : apis) {
                JSONObject obj = new JSONObject();
                obj.put("title", api.getApiName());
                obj.put("apiCode", api.getApiCode());
                obj.put("apiType", api.getApiType());
                obj.put("method", api.getMethod());
                obj.put("url", api.getUrl());
                relatedApis.add(obj);
            }
        }
        if (relatedFlows.isEmpty()) {
            List<FlowDefinition> flows = knowledgeService.searchFlows(userPrompt, 3);
            for (FlowDefinition f : flows) {
                JSONObject obj = new JSONObject();
                obj.put("flowCode", f.getFlowCode());
                obj.put("flowName", f.getFlowName());
                obj.put("triggerType", f.getTriggerType());
                obj.put("triggerConfig", f.getTriggerConfig());
                obj.put("executionMode", f.getExecutionMode());
                relatedFlows.add(obj);
            }
        }
        if (relatedDbSources.isEmpty()) {
            List<Datasource> datasources = knowledgeService.searchDatasources(userPrompt, 3);
            for (Datasource ds : datasources) {
                JSONObject obj = new JSONObject();
                obj.put("dsCode", ds.getDsCode());
                obj.put("dsName", ds.getDsName());
                obj.put("dbType", ds.getDbType());
                relatedDbSources.add(obj);
            }
        }

        variables.put("relatedApis", JSON.toJSONString(relatedApis));
        variables.put("relatedFlows", JSON.toJSONString(relatedFlows));
        variables.put("relatedDbSources", JSON.toJSONString(relatedDbSources));
        log.debug("AI 知识库检索完成: apis={}, flows={}, datasources={}",
                relatedApis.size(), relatedFlows.size(), relatedDbSources.size());
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

    /**
     * 当模型只返回 graphJson 时，从 graphJson 同步 nodes / edges，减少 prompt 长度
     */
    private void syncNodesEdgesFromGraphJson(AiGenerateFlowResponse response) {
        if (response == null || response.getGraphJson() == null) {
            return;
        }
        JSONObject graph = response.getGraphJson();
        if (response.getNodes() == null || response.getNodes().isEmpty()) {
            JSONArray nodes = graph.getJSONArray("nodes");
            if (nodes != null) {
                List<AiGenerateFlowResponse.FlowNodeDraft> list = new ArrayList<>();
                for (int i = 0; i < nodes.size(); i++) {
                    JSONObject n = nodes.getJSONObject(i);
                    AiGenerateFlowResponse.FlowNodeDraft draft = new AiGenerateFlowResponse.FlowNodeDraft();
                    draft.setNodeId(n.getString("id"));
                    draft.setNodeType(n.getString("type"));
                    draft.setConfigJson(JSON.toJSONString(n.getJSONObject("properties")));
                    list.add(draft);
                }
                response.setNodes(list);
            }
        }
        if (response.getEdges() == null) {
            JSONArray edges = graph.getJSONArray("edges");
            if (edges != null) {
                List<AiGenerateFlowResponse.FlowEdgeDraft> list = new ArrayList<>();
                for (int i = 0; i < edges.size(); i++) {
                    JSONObject e = edges.getJSONObject(i);
                    AiGenerateFlowResponse.FlowEdgeDraft draft = new AiGenerateFlowResponse.FlowEdgeDraft();
                    draft.setSourceNode(e.getString("sourceNodeId"));
                    draft.setTargetNode(e.getString("targetNodeId"));
                    JSONObject props = e.getJSONObject("properties");
                    draft.setConditionType(props != null ? props.getString("conditionType") : "default");
                    draft.setConditionExpression("");
                    draft.setPriority(0);
                    list.add(draft);
                }
                response.setEdges(list);
            }
        }
    }
}
