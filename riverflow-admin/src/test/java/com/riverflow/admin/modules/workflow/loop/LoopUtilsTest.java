package com.riverflow.admin.modules.workflow.loop;

import com.riverflow.api.entity.FlowEdge;
import com.riverflow.api.entity.FlowNode;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LoopUtils 单元测试
 */
class LoopUtilsTest {

    private FlowNode node(String id, String type) {
        FlowNode node = new FlowNode();
        node.setNodeId(id);
        node.setNodeType(type);
        return node;
    }

    private FlowEdge edge(String source, String target) {
        FlowEdge edge = new FlowEdge();
        edge.setSourceNode(source);
        edge.setTargetNode(target);
        return edge;
    }

    @Test
    void testFindLoopBodyNodes() {
        List<FlowNode> nodes = new ArrayList<>();
        nodes.add(node("start", "start"));
        nodes.add(node("foreach", "foreach"));
        nodes.add(node("api", "api"));
        nodes.add(node("script", "script"));
        nodes.add(node("end_foreach", "end_foreach"));
        nodes.add(node("end", "end"));

        List<FlowEdge> edges = new ArrayList<>();
        edges.add(edge("start", "foreach"));
        edges.add(edge("foreach", "api"));
        edges.add(edge("api", "script"));
        edges.add(edge("script", "end_foreach"));
        edges.add(edge("end_foreach", "end"));

        Set<String> body = LoopUtils.findLoopBodyNodes("foreach", "end_foreach", edges, nodes);
        assertEquals(2, body.size());
        assertTrue(body.contains("api"));
        assertTrue(body.contains("script"));
    }

    @Test
    void testResolveEndLoopNodeId() {
        List<FlowNode> nodes = new ArrayList<>();
        FlowNode foreach = node("foreach", "foreach");
        foreach.setConfigJson("{\"loopType\":\"foreach\"}");
        FlowNode endForeach = node("end_foreach", "end_foreach");
        endForeach.setConfigJson("{\"loopNodeId\":\"foreach\"}");
        nodes.add(foreach);
        nodes.add(endForeach);
        nodes.add(node("end", "end"));

        List<FlowEdge> edges = new ArrayList<>();
        edges.add(edge("foreach", "end_foreach"));
        edges.add(edge("end_foreach", "end"));

        String endNodeId = LoopUtils.resolveEndLoopNodeId("foreach", "foreach", nodes, edges);
        assertEquals("end_foreach", endNodeId);
    }

    @Test
    void testFindLoopPairs() {
        List<FlowNode> nodes = new ArrayList<>();
        FlowNode foreach = node("foreach", "foreach");
        FlowNode endForeach = node("end_foreach", "end_foreach");
        endForeach.setConfigJson("{\"loopNodeId\":\"foreach\"}");
        nodes.add(foreach);
        nodes.add(endForeach);

        List<FlowEdge> edges = new ArrayList<>();
        edges.add(edge("foreach", "end_foreach"));

        Map<String, String> pairs = LoopUtils.findLoopPairs(nodes, edges);
        assertEquals(1, pairs.size());
        assertEquals("end_foreach", pairs.get("foreach"));
    }
}
