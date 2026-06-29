package com.riverflow.admin.modules.workflow.loop;

import com.riverflow.admin.modules.workflow.context.FlowContext;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FlowContext 作用域与循环状态单元测试
 */
class FlowContextLoopTest {

    @Test
    void testScopeIsolation() {
        FlowContext context = new FlowContext();
        context.set("globalVar", "global");

        context.pushScope();
        context.set("item", "A");
        context.set("index", 0);

        assertEquals("A", context.get("item"));
        assertEquals(0, context.get("index"));
        assertEquals("global", context.get("globalVar"));

        context.popScope();
        assertNull(context.get("item"));
        assertEquals("global", context.get("globalVar"));
    }

    @Test
    void testNestedScope() {
        FlowContext context = new FlowContext();
        context.pushScope();
        context.set("item", "outer");

        context.pushScope();
        context.set("item", "inner");
        assertEquals("inner", context.get("item"));

        context.popScope();
        assertEquals("outer", context.get("item"));

        context.popScope();
    }

    @Test
    void testGlobalVariable() {
        FlowContext context = new FlowContext();
        context.setGlobal("loopResult", List.of("a", "b"));
        context.pushScope();
        assertEquals(List.of("a", "b"), context.get("loopResult"));
        context.popScope();
    }

    @Test
    void testLoopStateSerialization() {
        LoopState state = new LoopState();
        state.setLoopNodeId("foreach_001");
        state.setBodyEntryNodeId("api_001");
        state.setItems(List.of("a", "b", "c"));
        state.setTotal(3);
        state.setIndex(1);
        state.setResultVar("loopResult_foreach_001");
        state.setInitialized(true);

        Map<String, Object> map = state.toMap();
        LoopState restored = LoopState.from(map);

        assertNotNull(restored);
        assertEquals("foreach_001", restored.getLoopNodeId());
        assertEquals("api_001", restored.getBodyEntryNodeId());
        assertEquals(3, restored.getTotal());
        assertEquals(1, restored.getIndex());
        assertTrue(restored.isInitialized());
    }

    @Test
    void testLoopStateIdempotency() {
        LoopState state = new LoopState();
        state.setLoopNodeId("foreach_001");
        state.setIndex(0);
        state.markCurrentIndexAggregated();

        assertTrue(state.isCurrentIndexAggregated());
        state.nextIndex();
        assertFalse(state.isCurrentIndexAggregated());
    }

    @Test
    void testGlobalVariableUpdatedInLoopScope() {
        FlowContext context = new FlowContext();
        context.setGlobal("counter", 0);

        context.pushScope();
        context.set("counter", 1);
        assertEquals(1, context.get("counter"));

        context.popScope();
        assertEquals(1, context.getGlobal("counter"));
        assertEquals(1, context.get("counter"));
    }

    @Test
    void testLoopScopeLocalVariableNotPolluteGlobal() {
        FlowContext context = new FlowContext();
        context.setGlobal("counter", 0);

        context.pushScope();
        context.set("localTemp", 42);
        assertEquals(42, context.get("localTemp"));

        context.popScope();
        assertNull(context.get("localTemp"));
        assertEquals(0, context.get("counter"));
    }

    @Test
    void testForkContext() {
        FlowContext context = new FlowContext();
        context.setGlobal("shared", List.of("x"));

        FlowContext forked = context.fork();
        forked.setGlobal("shared", List.of("y"));

        assertEquals(List.of("x"), context.get("shared"));
        assertEquals(List.of("y"), forked.get("shared"));
    }
}
