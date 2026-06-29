package com.riverflow.admin.modules.workflow.loop;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * LoopState 单元测试
 */
class LoopStateTest {

    @Test
    void testSetResultByIndex() {
        LoopState state = new LoopState();
        state.setResult(2, "C");
        assertEquals(3, state.getResults().size());
        assertNull(state.getResults().get(0));
        assertNull(state.getResults().get(1));
        assertEquals("C", state.getResults().get(2));

        state.setResult(0, "A");
        assertEquals("A", state.getResults().get(0));
        assertEquals("C", state.getResults().get(2));
    }

    @Test
    void testLoopStateSerializationRoundTrip() {
        LoopState state = new LoopState();
        state.setLoopNodeId("foreach_1");
        state.setEndNodeId("end_foreach_1");
        state.setBodyEntryNodeId("node_1");
        state.setTotal(3);
        state.setIndex(1);
        state.setParallel(true);
        state.setBatchNo("BATCH_001");
        state.setResult(0, "A");
        state.setResult(1, "B");

        java.util.Map<String, Object> map = state.toMap();
        LoopState restored = LoopState.from(map);

        assertEquals("foreach_1", restored.getLoopNodeId());
        assertEquals("end_foreach_1", restored.getEndNodeId());
        assertEquals("node_1", restored.getBodyEntryNodeId());
        assertEquals(3, restored.getTotal());
        assertEquals(1, restored.getIndex());
        assertTrue(restored.isParallel());
        assertEquals("BATCH_001", restored.getBatchNo());
        assertEquals(Arrays.asList("A", "B"), restored.getResults());
    }
}
