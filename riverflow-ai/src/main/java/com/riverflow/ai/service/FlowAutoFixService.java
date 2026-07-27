package com.riverflow.ai.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.riverflow.ai.client.AiChatClient;
import com.riverflow.ai.config.AiProperties;
import com.riverflow.ai.dto.AiGenerateFlowRequest;
import com.riverflow.ai.dto.AiGenerateFlowResponse;
import com.riverflow.ai.parser.AiResponseParser;
import com.riverflow.ai.provider.AiChatRequest;
import com.riverflow.ai.provider.AiMessage;
import com.riverflow.api.entity.FlowEdge;
import com.riverflow.api.entity.FlowNode;
import com.riverflow.api.modules.workflow.simulate.FlowSimulationResult;
import com.riverflow.api.modules.workflow.validate.FlowValidationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 流程自动修复服务
 *
 * <p>当 AI 生成的流程在校验或沙箱模拟执行中失败时，最多进行 3 轮 LLM 自动修复闭环，
 * 将错误信息、执行路径和修复历史反馈给模型，要求其输出修复后的流程 JSON。
 */
@Slf4j
@Service
public class FlowAutoFixService {

    private static final String SCENE = "flow-auto-fix";
    private static final String DEFAULT_SYSTEM_PROMPT = "你是一个 RiverFlow 流程修复专家。你会收到一个 AI 生成的流程定义、校验错误和模拟执行错误。请分析并输出修复后的流程 JSON，保持原结构不变，仅修正问题。";

    private final AiChatClient aiChatClient;
    private final AiProperties aiProperties;
    private final FlowValidationAdapter flowValidationAdapter;
    private final FlowSimulationClient flowSimulationClient;
    private final AiResponseParser responseParser;

    @Autowired
    public FlowAutoFixService(AiChatClient aiChatClient, AiProperties aiProperties,
                              FlowValidationAdapter flowValidationAdapter,
                              FlowSimulationClient flowSimulationClient,
                              AiResponseParser responseParser) {
        this.aiChatClient = aiChatClient;
        this.aiProperties = aiProperties;
        this.flowValidationAdapter = flowValidationAdapter;
        this.flowSimulationClient = flowSimulationClient;
        this.responseParser = responseParser;
    }

    /**
     * 执行自动修复闭环
     *
     * @param response  原始生成结果（将被就地修改）
     * @param request   原始生成请求，用于取 extraContext 等配置
     * @param maxRounds 最大修复轮数（默认 3）
     * @return 修复后的结果（与入参同一对象）
     */
    public AiGenerateFlowResponse autoFix(AiGenerateFlowResponse response,
                                          AiGenerateFlowRequest request,
                                          int maxRounds) {
        if (response == null || response.getNodes() == null || response.getNodes().isEmpty()) {
            return response;
        }
        if (maxRounds <= 0) {
            maxRounds = aiProperties.getFlowGeneration().getAutoFixMaxRounds();
        }

        List<String> fixHistory = new ArrayList<>();
        int rounds = 0;
        boolean fixed = false;

        while (rounds < maxRounds) {
            rounds++;
            List<FlowNode> nodes = FlowNodeEdgeConverter.toFlowNodes(response.getNodes());
            List<FlowEdge> edges = FlowNodeEdgeConverter.toFlowEdges(response.getEdges());

            FlowValidationResult validationResult = flowValidationAdapter.validate(nodes, edges);
            FlowSimulationResult simulationResult = null;
            if (!request.isSkipSimulation() && validationResult.isValid()) {
                Map<String, Object> initialContext = request.getExtraContext() != null
                        ? request.getExtraContext() : new HashMap<>();
                simulationResult = flowSimulationClient.simulate(nodes, edges, initialContext);
            }

            response.setValidationResult(validationResult);
            response.setSimulationResult(simulationResult);

            boolean validationOk = validationResult.isValid();
            boolean simulationOk = simulationResult == null || simulationResult.isSuccess();

            if (validationOk && simulationOk) {
                fixed = true;
                fixHistory.add("第 " + rounds + " 轮：校验与模拟执行均通过");
                break;
            }

            String roundError = buildRoundError(rounds, validationResult, simulationResult);
            fixHistory.add(roundError);
            log.info("AI 流程自动修复第 {} 轮仍有错误: {}", rounds, roundError);

            if (rounds >= maxRounds) {
                break;
            }

            AiGenerateFlowResponse fixedResponse = callLlmToFix(response, request, validationResult, simulationResult, fixHistory);
            if (fixedResponse == null || fixedResponse.getNodes() == null || fixedResponse.getNodes().isEmpty()) {
                fixHistory.add("第 " + rounds + " 轮：LLM 未返回有效流程，停止修复");
                break;
            }
            applyFixedResponse(response, fixedResponse);
            fixHistory.add("第 " + rounds + " 轮：LLM 已返回修复后流程，进入下一轮校验");
        }

        response.setFixRounds(rounds);
        response.setFixHistory(fixHistory);
        response.setFullyRepaired(fixed);
        response.setReviewRequired(!fixed);
        return response;
    }

    private String buildRoundError(int round, FlowValidationResult validationResult,
                                   FlowSimulationResult simulationResult) {
        StringBuilder sb = new StringBuilder();
        sb.append("第 ").append(round).append(" 轮: ");
        if (validationResult != null && !validationResult.isValid()) {
            sb.append("校验错误: ");
            if (!CollectionUtils.isEmpty(validationResult.getErrors())) {
                sb.append(String.join("; ", validationResult.getErrors()));
            } else {
                sb.append("结构/语法/业务校验未通过");
            }
        }
        if (simulationResult != null && !simulationResult.isSuccess()) {
            if (sb.length() > 0) {
                sb.append(" | ");
            }
            sb.append("模拟执行错误: ").append(simulationResult.getErrorMsg());
        }
        return sb.toString();
    }

    private AiGenerateFlowResponse callLlmToFix(AiGenerateFlowResponse response,
                                                 AiGenerateFlowRequest request,
                                                 FlowValidationResult validationResult,
                                                 FlowSimulationResult simulationResult,
                                                 List<String> fixHistory) {
        String userPrompt = buildFixPrompt(response, validationResult, simulationResult, fixHistory);

        // 自动修复应继承主生成请求的 provider，未指定时使用默认 provider
        String provider = request != null && StringUtils.hasText(request.getProvider())
                ? request.getProvider() : aiProperties.getDefaultProvider();

        AiChatRequest chatRequest = AiChatRequest.builder()
                .model(request != null ? request.getModel() : null)
                .messages(List.of(
                        AiMessage.system(DEFAULT_SYSTEM_PROMPT),
                        AiMessage.user(userPrompt)
                ))
                .responseFormat("json_object")
                .scene(SCENE)
                .maxTokens(16384)
                .build();

        try {
            com.riverflow.ai.provider.AiChatResponse chatResponse = aiChatClient.chat(provider, chatRequest, "system");
            String content = responseParser.extractJson(chatResponse.getContent());
            if (!StringUtils.hasText(content)) {
                log.warn("LLM 修复流程未返回 JSON 内容");
                return null;
            }
            return responseParser.parseObject(content, AiGenerateFlowResponse.class);
        } catch (Exception e) {
            log.warn("LLM 修复流程调用失败", e);
            return null;
        }
    }

    private String buildFixPrompt(AiGenerateFlowResponse response,
                                  FlowValidationResult validationResult,
                                  FlowSimulationResult simulationResult,
                                  List<String> fixHistory) {
        JSONObject flowJson = new JSONObject();
        flowJson.put("flowName", response.getFlowName());
        flowJson.put("description", response.getDescription());
        flowJson.put("triggerType", response.getTriggerType());
        flowJson.put("triggerConfig", response.getTriggerConfig());
        flowJson.put("executionMode", response.getExecutionMode());
        flowJson.put("graphJson", response.getGraphJson());
        flowJson.put("nodes", response.getNodes());
        flowJson.put("edges", response.getEdges());

        JSONObject errorJson = new JSONObject();
        if (validationResult != null && !validationResult.isValid()) {
            errorJson.put("validationErrors", validationResult.getErrors());
            errorJson.put("validationWarnings", validationResult.getWarnings());
        }
        if (simulationResult != null && !simulationResult.isSuccess()) {
            errorJson.put("simulationError", simulationResult.getErrorMsg());
            if (!CollectionUtils.isEmpty(simulationResult.getSteps())) {
                List<String> stepLogs = new ArrayList<>();
                for (FlowSimulationResult.SimulationStep step : simulationResult.getSteps()) {
                    stepLogs.add(step.getNodeName() + "(" + step.getNodeType() + "): "
                            + (step.isSuccess() ? "成功" : "失败"));
                }
                errorJson.put("simulationSteps", stepLogs);
            }
        }

        JSONObject prompt = new JSONObject();
        prompt.put("originalFlow", flowJson);
        prompt.put("errors", errorJson);
        prompt.put("fixHistory", fixHistory);
        prompt.put("requirement", "请修复上述错误，输出完整的 AiGenerateFlowResponse JSON（包含 flowName, description, triggerType, triggerConfig, executionMode, graphJson, nodes, edges）。nodes 中每个节点需包含 nodeId, nodeType, configJson；edges 中每条边需包含 sourceNode, targetNode, conditionType, conditionExpression, priority。不要返回任何解释文字，只返回 JSON。");

        return prompt.toJSONString();
    }

    private void applyFixedResponse(AiGenerateFlowResponse target, AiGenerateFlowResponse fixed) {
        if (fixed == null) {
            return;
        }
        if (StringUtils.hasText(fixed.getFlowName())) {
            target.setFlowName(fixed.getFlowName());
        }
        if (StringUtils.hasText(fixed.getDescription())) {
            target.setDescription(fixed.getDescription());
        }
        if (StringUtils.hasText(fixed.getTriggerType())) {
            target.setTriggerType(fixed.getTriggerType());
        }
        if (StringUtils.hasText(fixed.getTriggerConfig())) {
            target.setTriggerConfig(fixed.getTriggerConfig());
        }
        if (StringUtils.hasText(fixed.getExecutionMode())) {
            target.setExecutionMode(fixed.getExecutionMode());
        }
        if (fixed.getGraphJson() != null) {
            target.setGraphJson(fixed.getGraphJson());
        }
        if (!CollectionUtils.isEmpty(fixed.getNodes())) {
            target.setNodes(fixed.getNodes());
        }
        if (fixed.getEdges() != null) {
            target.setEdges(fixed.getEdges());
        }
    }
}
