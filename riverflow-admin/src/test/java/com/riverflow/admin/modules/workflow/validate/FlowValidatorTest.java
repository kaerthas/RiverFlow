package com.riverflow.admin.modules.workflow.validate;

import com.riverflow.admin.modules.workflow.validate.rules.BusinessValidationRule;
import com.riverflow.admin.modules.workflow.validate.rules.GraphStructureValidationRule;
import com.riverflow.admin.modules.workflow.validate.rules.SyntaxValidationRule;
import com.riverflow.api.entity.FlowEdge;
import com.riverflow.api.entity.FlowNode;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 流程校验器单元测试
 */
class FlowValidatorTest {

    private FlowValidator buildValidator() {
        return new FlowValidator(Arrays.asList(
                new GraphStructureValidationRule(),
                new SyntaxValidationRule(),
                new BusinessValidationRule()
        ));
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
        return edge;
    }

    private FlowEdge edge(String source, String target, String conditionType, String expression) {
        FlowEdge edge = new FlowEdge();
        edge.setSourceNode(source);
        edge.setTargetNode(target);
        edge.setConditionType(conditionType);
        edge.setConditionExpression(expression);
        return edge;
    }

    @Test
    void testEmptyNodes() {
        FlowValidator validator = buildValidator();
        FlowValidationResult result = validator.validate(Collections.emptyList(), Collections.emptyList());
        assertFalse(result.isValid());
        assertTrue(result.getErrors().contains("流程节点为空"));
    }

    @Test
    void testMissingStartNode() {
        FlowValidator validator = buildValidator();
        List<FlowNode> nodes = Collections.singletonList(node("end1", "end"));
        FlowValidationResult result = validator.validate(nodes, Collections.emptyList());
        assertFalse(result.isValid());
        assertTrue(result.getErrors().contains("流程缺少开始节点"));
    }

    @Test
    void testMissingEndNode() {
        FlowValidator validator = buildValidator();
        List<FlowNode> nodes = Arrays.asList(node("start1", "start"), node("api1", "api"));
        FlowValidationResult result = validator.validate(nodes, Collections.emptyList());
        assertFalse(result.isValid());
        assertTrue(result.getErrors().contains("流程缺少结束节点"));
    }

    @Test
    void testValidLinearFlow() {
        FlowValidator validator = buildValidator();
        List<FlowNode> nodes = Arrays.asList(
                node("start1", "start"),
                node("api1", "api", "{\"apiCode\":\"test\"}"),
                node("end1", "end")
        );
        List<FlowEdge> edges = Arrays.asList(
                edge("start1", "api1"),
                edge("api1", "end1")
        );
        FlowValidationResult result = validator.validate(nodes, edges);
        assertTrue(result.isValid(), "Errors: " + result.getErrors());
    }

    @Test
    void testDuplicateNodeId() {
        FlowValidator validator = buildValidator();
        List<FlowNode> nodes = Arrays.asList(
                node("start1", "start"),
                node("start1", "api"),
                node("end1", "end")
        );
        List<FlowEdge> edges = Arrays.asList(
                edge("start1", "end1")
        );
        FlowValidationResult result = validator.validate(nodes, edges);
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("节点 ID 重复")),
                "Errors: " + result.getErrors());
    }

    @Test
    void testIllegalNodeType() {
        FlowValidator validator = buildValidator();
        List<FlowNode> nodes = Arrays.asList(
                node("start1", "start"),
                node("bad1", "unknown"),
                node("end1", "end")
        );
        List<FlowEdge> edges = Arrays.asList(
                edge("start1", "bad1"),
                edge("bad1", "end1")
        );
        FlowValidationResult result = validator.validate(nodes, edges);
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("类型") && e.contains("unknown")),
                "Errors: " + result.getErrors());
    }

    @Test
    void testDanglingEdge() {
        FlowValidator validator = buildValidator();
        List<FlowNode> nodes = Arrays.asList(
                node("start1", "start"),
                node("end1", "end")
        );
        List<FlowEdge> edges = Collections.singletonList(edge("start1", "missing"));
        FlowValidationResult result = validator.validate(nodes, edges);
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("不存在的目标节点")),
                "Errors: " + result.getErrors());
    }

    @Test
    void testUnreachableNode() {
        FlowValidator validator = buildValidator();
        List<FlowNode> nodes = Arrays.asList(
                node("start1", "start"),
                node("api1", "api", "{\"apiCode\":\"test\"}"),
                node("orphan", "api", "{\"apiCode\":\"test\"}"),
                node("end1", "end")
        );
        List<FlowEdge> edges = Arrays.asList(
                edge("start1", "api1"),
                edge("api1", "end1")
        );
        FlowValidationResult result = validator.validate(nodes, edges);
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("从开始节点无法到达")),
                "Errors: " + result.getErrors());
    }

    @Test
    void testCannotReachEnd() {
        FlowValidator validator = buildValidator();
        List<FlowNode> nodes = Arrays.asList(
                node("start1", "start"),
                node("api1", "api", "{\"apiCode\":\"test\"}"),
                node("dead", "api", "{\"apiCode\":\"test\"}"),
                node("end1", "end")
        );
        List<FlowEdge> edges = Arrays.asList(
                edge("start1", "api1"),
                edge("api1", "dead")
        );
        FlowValidationResult result = validator.validate(nodes, edges);
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("无法到达任何结束节点")),
                "Errors: " + result.getErrors());
    }

    @Test
    void testDbPlaceholderMissing() {
        FlowValidator validator = buildValidator();
        List<FlowNode> nodes = Arrays.asList(
                node("start1", "start"),
                node("db1", "db", "{\"sql\":\"select * from t where id = #{userId}\"}"),
                node("end1", "end")
        );
        List<FlowEdge> edges = Arrays.asList(
                edge("start1", "db1"),
                edge("db1", "end1")
        );
        FlowValidationResult result = validator.validate(nodes, edges);
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("占位符") && e.contains("userId")),
                "Errors: " + result.getErrors());
    }

    @Test
    void testDbPlaceholderConfigured() {
        FlowValidator validator = buildValidator();
        FlowNode dbNode = node("db1", "db", "{\"sql\":\"select * from t where id = #{userId}\"}");
        dbNode.setInputMapping("[{\"source\":\"context.userId\",\"target\":\"userId\"}]");
        List<FlowNode> nodes = Arrays.asList(
                node("start1", "start"),
                dbNode,
                node("end1", "end")
        );
        List<FlowEdge> edges = Arrays.asList(
                edge("start1", "db1"),
                edge("db1", "end1")
        );
        FlowValidationResult result = validator.validate(nodes, edges);
        assertTrue(result.isValid(), "Errors: " + result.getErrors());
    }

    @Test
    void testInvalidSpelExpression() {
        FlowValidator validator = buildValidator();
        List<FlowNode> nodes = Arrays.asList(
                node("start1", "start"),
                node("cond1", "condition", "{\"conditionExpression\":\"#{context.value >>>}\"}"),
                node("end1", "end")
        );
        List<FlowEdge> edges = Arrays.asList(
                edge("start1", "cond1"),
                edge("cond1", "end1")
        );
        FlowValidationResult result = validator.validate(nodes, edges);
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("SpEL") || e.contains("条件")),
                "Errors: " + result.getErrors());
    }

    @Test
    void testInvalidGroovyScript() {
        FlowValidator validator = buildValidator();
        List<FlowNode> nodes = Arrays.asList(
                node("start1", "start"),
                node("script1", "script", "{\"script\":\"def a = \\n  def b =\"}"),
                node("end1", "end")
        );
        List<FlowEdge> edges = Arrays.asList(
                edge("start1", "script1"),
                edge("script1", "end1")
        );
        FlowValidationResult result = validator.validate(nodes, edges);
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("Groovy")),
                "Errors: " + result.getErrors());
    }

    @Test
    void testInvalidCronExpression() {
        FlowValidator validator = buildValidator();
        List<FlowNode> nodes = Arrays.asList(
                node("start1", "start"),
                node("timer1", "timer", "{\"cronExpression\":\"invalid cron\"}"),
                node("end1", "end")
        );
        List<FlowEdge> edges = Arrays.asList(
                edge("start1", "timer1"),
                edge("timer1", "end1")
        );
        FlowValidationResult result = validator.validate(nodes, edges);
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("Cron")),
                "Errors: " + result.getErrors());
    }

    @Test
    void testInvalidMappingJson() {
        FlowValidator validator = buildValidator();
        FlowNode apiNode = node("api1", "api", "{\"apiCode\":\"test\"}");
        apiNode.setInputMapping("not a json array");
        List<FlowNode> nodes = Arrays.asList(
                node("start1", "start"),
                apiNode,
                node("end1", "end")
        );
        List<FlowEdge> edges = Arrays.asList(
                edge("start1", "api1"),
                edge("api1", "end1")
        );
        FlowValidationResult result = validator.validate(nodes, edges);
        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream().anyMatch(e -> e.contains("inputMapping")),
                "Errors: " + result.getErrors());
    }

    @Test
    void testDbBuiltInPlaceholderPassed() {
        FlowValidator validator = buildValidator();
        FlowNode dbNode = node("db1", "db", "{\"sql\":\"select * from t where business_key = #{_businessKey}\"}");
        List<FlowNode> nodes = Arrays.asList(
                node("start1", "start"),
                dbNode,
                node("end1", "end")
        );
        List<FlowEdge> edges = Arrays.asList(
                edge("start1", "db1"),
                edge("db1", "end1")
        );
        FlowValidationResult result = validator.validate(nodes, edges);
        assertTrue(result.isValid(), "Errors: " + result.getErrors());
    }

    @Test
    void testBackwardCompatibleValidateNodesOnly() {
        FlowValidator validator = buildValidator();
        List<FlowNode> nodes = Arrays.asList(
                node("start1", "start"),
                node("db1", "db", "{\"sql\":\"select * from t where id = #{userId}\"}"),
                node("end1", "end")
        );
        List<String> errors = validator.validate(nodes);
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(e -> e.contains("占位符")),
                "Errors: " + errors);
    }
}
