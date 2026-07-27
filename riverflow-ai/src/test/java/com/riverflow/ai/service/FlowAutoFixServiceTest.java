package com.riverflow.ai.service;

import com.riverflow.ai.client.AiChatClient;
import com.riverflow.ai.config.AiProperties;
import com.riverflow.ai.dto.AiGenerateFlowRequest;
import com.riverflow.ai.dto.AiGenerateFlowResponse;
import com.riverflow.ai.parser.AiResponseParser;
import com.riverflow.ai.provider.AiChatResponse;
import com.riverflow.api.modules.workflow.simulate.FlowSimulationResult;
import com.riverflow.api.modules.workflow.validate.FlowValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * AI 流程自动修复服务单元测试
 */
class FlowAutoFixServiceTest {

    private FlowAutoFixService flowAutoFixService;
    private AiChatClient aiChatClient;
    private AiProperties aiProperties;
    private FlowValidationAdapter flowValidationAdapter;
    private FlowSimulationClient flowSimulationClient;
    private AiResponseParser responseParser;

    @BeforeEach
    void setUp() {
        aiChatClient = mock(AiChatClient.class);
        aiProperties = new AiProperties();
        aiProperties.setDefaultProvider("mock-provider");
        aiProperties.getFlowGeneration().setAutoFixEnabled(true);
        aiProperties.getFlowGeneration().setAutoFixMaxRounds(3);
        flowValidationAdapter = mock(FlowValidationAdapter.class);
        flowSimulationClient = mock(FlowSimulationClient.class);
        responseParser = mock(AiResponseParser.class);

        flowAutoFixService = new FlowAutoFixService(
                aiChatClient, aiProperties, flowValidationAdapter, flowSimulationClient, responseParser
        );
    }

    private AiGenerateFlowResponse buildResponse() {
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

    private AiGenerateFlowRequest buildRequest() {
        AiGenerateFlowRequest request = new AiGenerateFlowRequest();
        request.setUserPrompt("测试");
        request.setSkipSimulation(false);
        return request;
    }

    private FlowValidationResult validResult() {
        FlowValidationResult result = new FlowValidationResult();
        result.setValid(true);
        return result;
    }

    private FlowValidationResult invalidResult(String error) {
        FlowValidationResult result = new FlowValidationResult();
        result.setValid(false);
        result.getErrors().add(error);
        return result;
    }

    private FlowSimulationResult successSimulation() {
        FlowSimulationResult result = new FlowSimulationResult();
        result.setSuccess(true);
        return result;
    }

    private FlowSimulationResult failSimulation(String error) {
        FlowSimulationResult result = new FlowSimulationResult();
        result.setSuccess(false);
        result.setErrorMsg(error);
        return result;
    }

    private void mockLlmFix(AiGenerateFlowResponse fixed) {
        when(responseParser.extractJson(anyString())).thenReturn("{}");
        when(responseParser.parseObject(anyString(), any())).thenReturn(fixed);
        when(aiChatClient.chat(any(), any(), anyString())).thenReturn(
                AiChatResponse.builder().content("{}").build()
        );
    }

    @Test
    void testNoFixNeeded() {
        AiGenerateFlowResponse response = buildResponse();
        AiGenerateFlowRequest request = buildRequest();
        when(flowValidationAdapter.validate(any(), any())).thenReturn(validResult());
        when(flowSimulationClient.simulate(any(), any(), any())).thenReturn(successSimulation());

        AiGenerateFlowResponse result = flowAutoFixService.autoFix(response, request, 3);

        assertEquals(1, result.getFixRounds());
        assertTrue(result.isFullyRepaired());
        assertTrue(result.getFixHistory().get(0).contains("通过"));
    }

    @Test
    void testFixSuccessAtSecondRound() {
        AiGenerateFlowResponse response = buildResponse();
        AiGenerateFlowRequest request = buildRequest();

        when(flowValidationAdapter.validate(any(), any()))
                .thenReturn(invalidResult("缺少结束节点"))
                .thenReturn(validResult());
        when(flowSimulationClient.simulate(any(), any(), any())).thenReturn(successSimulation());

        AiGenerateFlowResponse fixed = buildResponse();
        mockLlmFix(fixed);

        AiGenerateFlowResponse result = flowAutoFixService.autoFix(response, request, 3);

        assertEquals(2, result.getFixRounds());
        assertTrue(result.isFullyRepaired());
        assertTrue(result.getValidationResult().isValid());
    }

    @Test
    void testReachMaxRounds() {
        AiGenerateFlowResponse response = buildResponse();
        AiGenerateFlowRequest request = buildRequest();

        when(flowValidationAdapter.validate(any(), any()))
                .thenReturn(invalidResult("始终失败"));
        AiGenerateFlowResponse fixed = buildResponse();
        mockLlmFix(fixed);

        AiGenerateFlowResponse result = flowAutoFixService.autoFix(response, request, 2);

        assertEquals(2, result.getFixRounds());
        assertFalse(result.isFullyRepaired());
        assertNotNull(result.getFixHistory());
        assertEquals(3, result.getFixHistory().size()); // 第1轮错误 + LLM修复 + 第2轮错误
    }

    @Test
    void testSkipSimulationWhenInvalid() {
        AiGenerateFlowResponse response = buildResponse();
        AiGenerateFlowRequest request = buildRequest();

        when(flowValidationAdapter.validate(any(), any()))
                .thenReturn(invalidResult("缺少结束节点"))
                .thenReturn(validResult());
        when(flowSimulationClient.simulate(any(), any(), any())).thenReturn(successSimulation());

        AiGenerateFlowResponse fixed = buildResponse();
        mockLlmFix(fixed);

        AiGenerateFlowResponse result = flowAutoFixService.autoFix(response, request, 3);

        // 第一轮校验失败时不会调用模拟
        assertTrue(result.isFullyRepaired());
    }
}
