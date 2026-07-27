package com.riverflow.admin.modules.workflow.simulate;

import com.riverflow.api.modules.workflow.simulate.FlowSimulationResult;
import com.riverflow.admin.infra.groovy.GroovySandboxExecutor;
import com.riverflow.api.entity.FlowEdge;
import com.riverflow.api.entity.FlowNode;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 流程沙箱模拟执行引擎单元测试
 */
class FlowSimulationEngineTest {

    private FlowSimulationEngine buildEngine() {
        FlowSimulationEngine engine = new FlowSimulationEngine();
        GroovySandboxExecutor groovyExecutor = new GroovySandboxExecutor();
        ReflectionTestUtils.setField(groovyExecutor, "redisTemplate", new MockRedisTemplate());
        ReflectionTestUtils.setField(groovyExecutor, "scriptTimeoutSeconds", 5);
        engine.setGroovySandboxExecutor(groovyExecutor);
        return engine;
    }

    private FlowNode node(String id, String type) {
        FlowNode node = new FlowNode();
        node.setNodeId(id);
        node.setNodeName(id);
        node.setNodeType(type);
        return node;
    }

    private FlowNode node(String id, String type, String configJson) {
        FlowNode node = node(id, type);
        node.setConfigJson(configJson);
        return node;
    }

    private FlowEdge edge(String source, String target) {
        FlowEdge edge = new FlowEdge();
        edge.setSourceNode(source);
        edge.setTargetNode(target);
        edge.setConditionType("default");
        edge.setPriority(0);
        return edge;
    }

    private FlowEdge edge(String source, String target, String conditionType, String expression) {
        FlowEdge edge = new FlowEdge();
        edge.setSourceNode(source);
        edge.setTargetNode(target);
        edge.setConditionType(conditionType);
        edge.setConditionExpression(expression);
        edge.setPriority(0);
        return edge;
    }

    @Test
    void testEmptyNodes() {
        FlowSimulationEngine engine = buildEngine();
        FlowSimulationResult result = engine.simulate(Collections.emptyList(), Collections.emptyList(), null);
        assertFalse(result.isSuccess());
        assertEquals("流程节点为空", result.getErrorMsg());
    }

    @Test
    void testSimpleStartToEnd() {
        List<FlowNode> nodes = Arrays.asList(
                node("start_1", "start"),
                node("end_1", "end")
        );
        List<FlowEdge> edges = Collections.singletonList(edge("start_1", "end_1"));
        FlowSimulationEngine engine = buildEngine();
        FlowSimulationResult result = engine.simulate(nodes, edges, null);
        assertTrue(result.isSuccess());
        assertTrue(result.isReachedEnd());
        assertEquals(2, result.getSteps().size());
    }

    @Test
    void testScriptNode() {
        List<FlowNode> nodes = Arrays.asList(
                node("start_1", "start"),
                node("script_1", "script", "{ \"scriptContent\": \"return [msg: 'hello'] \" }"),
                node("end_1", "end")
        );
        List<FlowEdge> edges = Arrays.asList(
                edge("start_1", "script_1"),
                edge("script_1", "end_1")
        );
        FlowSimulationEngine engine = buildEngine();
        FlowSimulationResult result = engine.simulate(nodes, edges, null);
        assertTrue(result.isSuccess());
        assertEquals(3, result.getSteps().size());
        FlowSimulationResult.SimulationStep scriptStep = result.getSteps().get(1);
        assertTrue(scriptStep.isSuccess());
        assertNotNull(scriptStep.getResultData());
    }

    @Test
    void testDbNodeMock() {
        List<FlowNode> nodes = Arrays.asList(
                node("start_1", "start"),
                node("db_1", "db", "{ \"sql\": \"SELECT * FROM t WHERE id = #{id}\", \"operation\": \"select\", \"resultVarName\": \"data\" }"),
                node("end_1", "end")
        );
        FlowNode dbNode = nodes.get(1);
        dbNode.setInputMapping("[{\"source\": \"context.id\", \"target\": \"id\", \"type\": \"const\"}]");
        List<FlowEdge> edges = Arrays.asList(
                edge("start_1", "db_1"),
                edge("db_1", "end_1")
        );
        FlowSimulationEngine engine = buildEngine();
        FlowSimulationResult result = engine.simulate(nodes, edges, Map.of("id", "123"));
        assertTrue(result.isSuccess());
        FlowSimulationResult.SimulationStep dbStep = result.getSteps().get(1);
        assertTrue(dbStep.isSuccess());
        Map<?, ?> data = (Map<?, ?>) dbStep.getResultData();
        assertTrue(Boolean.parseBoolean(data.get("mock").toString()));
    }

    @Test
    void testApiNodeMock() {
        List<FlowNode> nodes = Arrays.asList(
                node("start_1", "start"),
                node("api_1", "api", "{ \"apiCode\": \"test-api\" }"),
                node("end_1", "end")
        );
        List<FlowEdge> edges = Arrays.asList(
                edge("start_1", "api_1"),
                edge("api_1", "end_1")
        );
        FlowSimulationEngine engine = buildEngine();
        FlowSimulationResult result = engine.simulate(nodes, edges, null);
        assertTrue(result.isSuccess());
        FlowSimulationResult.SimulationStep apiStep = result.getSteps().get(1);
        assertTrue(apiStep.isSuccess());
    }

    @Test
    void testCustomConditionEdge() {
        List<FlowNode> nodes = Arrays.asList(
                node("start_1", "start"),
                node("condition_1", "condition", "{ \"conditionExpression\": \"#{context.pass} == true\" }"),
                node("end_1", "end")
        );
        List<FlowEdge> edges = Arrays.asList(
                edge("start_1", "condition_1"),
                edge("condition_1", "end_1", "custom", "#{context.pass == true}")
        );
        FlowSimulationEngine engine = buildEngine();
        FlowSimulationResult result = engine.simulate(nodes, edges, Map.of("pass", true));
        assertTrue(result.isSuccess());
        assertTrue(result.isReachedEnd());
    }

    @Test
    void testDbNodeMissingPlaceholderMapping() {
        List<FlowNode> nodes = Arrays.asList(
                node("start_1", "start"),
                node("db_1", "db", "{ \"sql\": \"SELECT * FROM t WHERE id = #{id}\", \"operation\": \"select\" }"),
                node("end_1", "end")
        );
        List<FlowEdge> edges = Arrays.asList(
                edge("start_1", "db_1"),
                edge("db_1", "end_1")
        );
        FlowSimulationEngine engine = buildEngine();
        FlowSimulationResult result = engine.simulate(nodes, edges, null);
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMsg().contains("SQL 占位符"));
    }

    @Test
    void testMissingEndNode() {
        List<FlowNode> nodes = Arrays.asList(
                node("start_1", "start"),
                node("script_1", "script", "{ \"scriptContent\": \"return [:] \" }")
        );
        List<FlowEdge> edges = Collections.singletonList(edge("start_1", "script_1"));
        FlowSimulationEngine engine = buildEngine();
        FlowSimulationResult result = engine.simulate(nodes, edges, null);
        assertFalse(result.isSuccess());
        assertTrue(result.getErrorMsg().contains("无有效流转"));
    }

    /**
     * 简化版 Mock RedisTemplate，用于 GroovySandboxExecutor 注入
     */
    private static class MockRedisTemplate extends StringRedisTemplate {
    }
}
