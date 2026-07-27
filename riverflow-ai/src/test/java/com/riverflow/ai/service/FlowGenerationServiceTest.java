package com.riverflow.ai.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.riverflow.ai.audit.AiAuditLogService;
import com.riverflow.ai.client.AiChatClient;
import com.riverflow.ai.config.AiProperties;
import com.riverflow.ai.dto.AiGenerateFlowRequest;
import com.riverflow.ai.dto.AiGenerateFlowResponse;
import com.riverflow.ai.knowledge.service.AiKnowledgeService;
import com.riverflow.ai.parser.AiJsonSchemaValidator;
import com.riverflow.ai.parser.AiOutputValidator;
import com.riverflow.ai.parser.AiResponseParser;
import com.riverflow.ai.prompt.PromptTemplateEngine;
import com.riverflow.ai.prompt.PromptTemplateLoader;
import com.riverflow.ai.prompt.dto.PromptContent;
import com.riverflow.ai.provider.AiChatRequest;
import com.riverflow.ai.provider.AiChatResponse;
import com.riverflow.api.entity.FlowEdge;
import com.riverflow.api.entity.FlowNode;
import com.riverflow.api.modules.workflow.simulate.FlowSimulationResult;
import com.riverflow.api.modules.workflow.validate.FlowValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * AI 流程生成服务单元测试
 *
 * <p>重点覆盖生成后自动接入的流程校验与沙箱模拟执行链路。
 */
class FlowGenerationServiceTest {

    private FlowGenerationService flowGenerationService;
    private AiChatClient aiChatClient;
    private AiProperties aiProperties;
    private AiAuditLogService auditLogService;
    private PromptTemplateEngine templateEngine;
    private PromptTemplateLoader templateLoader;
    private AiResponseParser responseParser;
    private AiOutputValidator outputValidator;
    private AiJsonSchemaValidator schemaValidator;
    private AiKnowledgeService knowledgeService;
    private FlowValidationAdapter flowValidationAdapter;
    private FlowSimulationClient flowSimulationClient;
    private FlowAutoFixService flowAutoFixService;

    @BeforeEach
    void setUp() {
        aiChatClient = mock(AiChatClient.class);
        aiProperties = new AiProperties();
        aiProperties.setAuditEnabled(false);
        auditLogService = mock(AiAuditLogService.class);
        templateEngine = mock(PromptTemplateEngine.class);
        templateLoader = mock(PromptTemplateLoader.class);
        responseParser = mock(AiResponseParser.class);
        outputValidator = mock(AiOutputValidator.class);
        schemaValidator = mock(AiJsonSchemaValidator.class);
        knowledgeService = mock(AiKnowledgeService.class);
        flowValidationAdapter = mock(FlowValidationAdapter.class);
        flowSimulationClient = mock(FlowSimulationClient.class);
        flowAutoFixService = mock(FlowAutoFixService.class);

        flowGenerationService = new FlowGenerationService(
                aiChatClient, aiProperties, auditLogService, templateEngine, templateLoader,
                responseParser, outputValidator, schemaValidator, knowledgeService,
                flowValidationAdapter, flowSimulationClient, flowAutoFixService
        );
    }

    private AiGenerateFlowRequest buildRequest(boolean skipSimulation) {
        AiGenerateFlowRequest request = new AiGenerateFlowRequest();
        request.setUserPrompt("测试生成流程");
        request.setSkipSimulation(skipSimulation);
        return request;
    }

    private AiGenerateFlowResponse buildResponseWithNodes() {
        AiGenerateFlowResponse response = new AiGenerateFlowResponse();
        response.setFlowName("测试流程");

        List<AiGenerateFlowResponse.FlowNodeDraft> nodes = new ArrayList<>();
        AiGenerateFlowResponse.FlowNodeDraft start = new AiGenerateFlowResponse.FlowNodeDraft();
        start.setNodeId("start_1");
        start.setNodeType("start");
        nodes.add(start);

        AiGenerateFlowResponse.FlowNodeDraft end = new AiGenerateFlowResponse.FlowNodeDraft();
        end.setNodeId("end_1");
        end.setNodeType("end");
        nodes.add(end);
        response.setNodes(nodes);

        List<AiGenerateFlowResponse.FlowEdgeDraft> edges = new ArrayList<>();
        AiGenerateFlowResponse.FlowEdgeDraft edge = new AiGenerateFlowResponse.FlowEdgeDraft();
        edge.setSourceNode("start_1");
        edge.setTargetNode("end_1");
        edges.add(edge);
        response.setEdges(edges);

        return response;
    }

    private void mockGenerateDependencies(AiGenerateFlowResponse response) {
        PromptContent promptContent = new PromptContent();
        promptContent.setScene("flow-generation");
        promptContent.setModel("default");
        promptContent.setVersion("v1");
        promptContent.setTemplate("template");
        promptContent.setSystemPrompt("system");
        when(templateLoader.load(eq("flow-generation"), eq("default"), any())).thenReturn(promptContent);
        when(templateEngine.render(anyString(), any())).thenReturn("rendered prompt");

        AiChatResponse chatResponse = AiChatResponse.builder()
                .content("{}")
                .promptTokens(10)
                .completionTokens(20)
                .build();
        when(aiChatClient.chat(any(AiChatRequest.class), anyString())).thenReturn(chatResponse);

        when(responseParser.extractThink(anyString())).thenReturn("thinking");
        when(responseParser.parseObject(anyString(), eq(AiGenerateFlowResponse.class))).thenReturn(response);
        doNothing().when(outputValidator).validate(any(AiGenerateFlowResponse.class));

        when(knowledgeService.searchSemanticGrouped(anyString(), any(), any(), any()))
                .thenReturn(Collections.emptyMap());
    }

    @Test
    void testValidationFailure() {
        AiGenerateFlowRequest request = buildRequest(false);
        AiGenerateFlowResponse response = buildResponseWithNodes();
        mockGenerateDependencies(response);

        FlowValidationResult validationResult = new FlowValidationResult();
        validationResult.setValid(false);
        validationResult.getErrors().add("缺少结束节点");
        when(flowValidationAdapter.validate(any(List.class), any(List.class))).thenReturn(validationResult);

        AiGenerateFlowResponse result = flowGenerationService.generate(request, "test-user");

        assertNotNull(result.getValidationResult());
        assertFalse(result.getValidationResult().isValid());
        assertTrue(result.isReviewRequired());
        assertNull(result.getSimulationResult());
        verify(flowSimulationClient, never()).simulate(any(List.class), any(List.class), any());
    }

    @Test
    void testSimulationFailure() {
        AiGenerateFlowRequest request = buildRequest(false);
        AiGenerateFlowResponse response = buildResponseWithNodes();
        mockGenerateDependencies(response);

        FlowValidationResult validationResult = new FlowValidationResult();
        validationResult.setValid(true);
        when(flowValidationAdapter.validate(any(List.class), any(List.class))).thenReturn(validationResult);

        FlowSimulationResult simulationResult = new FlowSimulationResult();
        simulationResult.setSuccess(false);
        simulationResult.setErrorMsg("模拟执行失败");
        when(flowSimulationClient.simulate(any(List.class), any(List.class), any())).thenReturn(simulationResult);

        AiGenerateFlowResponse result = flowGenerationService.generate(request, "test-user");

        assertNotNull(result.getValidationResult());
        assertTrue(result.getValidationResult().isValid());
        assertNotNull(result.getSimulationResult());
        assertFalse(result.getSimulationResult().isSuccess());
        assertTrue(result.isReviewRequired());
    }

    @Test
    void testValidationAndSimulationSuccess() {
        AiGenerateFlowRequest request = buildRequest(false);
        AiGenerateFlowResponse response = buildResponseWithNodes();
        mockGenerateDependencies(response);

        FlowValidationResult validationResult = new FlowValidationResult();
        validationResult.setValid(true);
        when(flowValidationAdapter.validate(any(List.class), any(List.class))).thenReturn(validationResult);

        FlowSimulationResult simulationResult = new FlowSimulationResult();
        simulationResult.setSuccess(true);
        simulationResult.setReachedEnd(true);
        when(flowSimulationClient.simulate(any(List.class), any(List.class), any())).thenReturn(simulationResult);

        AiGenerateFlowResponse result = flowGenerationService.generate(request, "test-user");

        assertNotNull(result.getValidationResult());
        assertTrue(result.getValidationResult().isValid());
        assertNotNull(result.getSimulationResult());
        assertTrue(result.getSimulationResult().isSuccess());
        assertFalse(result.isReviewRequired());
    }

    @Test
    void testSkipSimulation() {
        AiGenerateFlowRequest request = buildRequest(true);
        AiGenerateFlowResponse response = buildResponseWithNodes();
        mockGenerateDependencies(response);

        FlowValidationResult validationResult = new FlowValidationResult();
        validationResult.setValid(true);
        when(flowValidationAdapter.validate(any(List.class), any(List.class))).thenReturn(validationResult);

        AiGenerateFlowResponse result = flowGenerationService.generate(request, "test-user");

        assertNotNull(result.getValidationResult());
        assertTrue(result.getValidationResult().isValid());
        assertNull(result.getSimulationResult());
        assertFalse(result.isReviewRequired());
        verify(flowSimulationClient, never()).simulate(any(List.class), any(List.class), any());
    }
}
