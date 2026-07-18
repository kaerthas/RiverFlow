package com.riverflow.ai.knowledge.chunk;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 默认文档分块器单元测试
 */
class DefaultDocumentChunkerTest {

    private final DefaultDocumentChunker chunker = new DefaultDocumentChunker();

    @Test
    void testEmptyText() {
        List<String> chunks = chunker.chunk("");
        assertTrue(chunks.isEmpty());
    }

    @Test
    void testSimpleChunk() {
        ChunkOptions options = new ChunkOptions();
        options.setSize(10);
        options.setOverlap(2);
        options.setMaxChunks(10);

        String text = "这是一段用于测试分块功能的中文文本，需要能够被正确切分成多个块。";
        List<String> chunks = chunker.chunk(text, options);

        assertTrue(chunks.size() > 1, "长文本应被切分为多个块");
        // 重叠检查：相邻块应有公共前缀
        for (int i = 1; i < chunks.size(); i++) {
            String prev = chunks.get(i - 1);
            String curr = chunks.get(i);
            boolean hasOverlap = prev.endsWith(curr.substring(0, Math.min(curr.length(), options.getOverlap())))
                    || curr.startsWith(prev.substring(Math.max(0, prev.length() - options.getOverlap())));
            assertTrue(hasOverlap, "相邻块应存在重叠");
        }
    }

    @Test
    void testMaxChunksLimit() {
        ChunkOptions options = new ChunkOptions();
        options.setSize(5);
        options.setOverlap(1);
        options.setMaxChunks(2);

        String text = "这是一个非常非常非常非常非常非常非常非常长的文本，应该被限制最大分块数。";
        List<String> chunks = chunker.chunk(text, options);
        assertEquals(2, chunks.size(), "应受最大分块数限制");
    }
}
