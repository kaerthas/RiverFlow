package com.riverflow.ai.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.riverflow.ai.dto.AiGenerateFlowResponse;
import com.riverflow.api.entity.FlowEdge;
import com.riverflow.api.entity.FlowNode;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * AI 生成流程草稿与 API 流程实体之间的转换器
 *
 * <p>将 {@link AiGenerateFlowResponse.FlowNodeDraft} / {@link AiGenerateFlowResponse.FlowEdgeDraft}
 * 转换为 {@link FlowNode} / {@link FlowEdge}，供校验与沙箱模拟执行使用。
 */
public class FlowNodeEdgeConverter {

    /**
     * 将节点草稿转换为 FlowNode
     *
     * @param drafts 节点草稿列表
     * @return FlowNode 列表
     */
    public static List<FlowNode> toFlowNodes(List<AiGenerateFlowResponse.FlowNodeDraft> drafts) {
        List<FlowNode> nodes = new ArrayList<>();
        if (drafts == null) {
            return nodes;
        }
        int sortNo = 0;
        for (AiGenerateFlowResponse.FlowNodeDraft draft : drafts) {
            FlowNode node = new FlowNode();
            node.setNodeId(draft.getNodeId());
            node.setNodeType(draft.getNodeType());
            node.setNodeName(extractNodeName(draft));
            node.setConfigJson(draft.getConfigJson());
            node.setSortNo(sortNo++);
            nodes.add(node);
        }
        return nodes;
    }

    /**
     * 将边草稿转换为 FlowEdge
     *
     * @param drafts 边草稿列表
     * @return FlowEdge 列表
     */
    public static List<FlowEdge> toFlowEdges(List<AiGenerateFlowResponse.FlowEdgeDraft> drafts) {
        List<FlowEdge> edges = new ArrayList<>();
        if (drafts == null) {
            return edges;
        }
        int priority = 0;
        for (AiGenerateFlowResponse.FlowEdgeDraft draft : drafts) {
            FlowEdge edge = new FlowEdge();
            edge.setSourceNode(draft.getSourceNode());
            edge.setTargetNode(draft.getTargetNode());
            edge.setConditionType(StringUtils.hasText(draft.getConditionType()) ? draft.getConditionType() : "default");
            edge.setConditionExpression(draft.getConditionExpression());
            edge.setPriority(draft.getPriority() != null ? draft.getPriority() : priority++);
            edges.add(edge);
        }
        return edges;
    }

    private static String extractNodeName(AiGenerateFlowResponse.FlowNodeDraft draft) {
        if (draft == null) {
            return null;
        }
        if (StringUtils.hasText(draft.getNodeId())) {
            return draft.getNodeId();
        }
        if (!StringUtils.hasText(draft.getConfigJson())) {
            return null;
        }
        try {
            JSONObject config = JSON.parseObject(draft.getConfigJson());
            String name = config.getString("name");
            if (StringUtils.hasText(name)) {
                return name;
            }
        } catch (Exception ignored) {
        }
        return draft.getNodeType();
    }
}
