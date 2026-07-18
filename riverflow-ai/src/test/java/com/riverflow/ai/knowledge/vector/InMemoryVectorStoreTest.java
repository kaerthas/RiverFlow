package com.riverflow.ai.knowledge.vector;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 内存向量存储单元测试
 */
class InMemoryVectorStoreTest {

    private InMemoryVectorStore store;

    @BeforeEach
    void setUp() {
        store = new InMemoryVectorStore();
        store.createCollection("test", 3, DistanceMetric.COSINE);
    }

    @Test
    void testUpsertAndSearch() {
        VectorDocument doc1 = buildDoc("1", "doc1", 0, new float[]{1, 0, 0}, "苹果和橘子");
        VectorDocument doc2 = buildDoc("2", "doc1", 1, new float[]{0, 1, 0}, "香蕉和菠萝");
        store.upsert("test", Arrays.asList(doc1, doc2));

        List<VectorDocument> results = store.search("test", new float[]{1, 0, 0}, 2, 0.0);
        assertEquals(2, results.size());
        assertEquals("1", results.get(0).getId());
        assertTrue(results.get(0).getScore() > results.get(1).getScore());
    }

    @Test
    void testMinScoreFilter() {
        VectorDocument doc = buildDoc("1", "doc1", 0, new float[]{1, 0, 0}, "测试文档");
        store.upsert("test", List.of(doc));

        List<VectorDocument> results = store.search("test", new float[]{0, 1, 0}, 2, 0.9);
        assertTrue(results.isEmpty(), "低于阈值的文档应被过滤");
    }

    @Test
    void testDeleteByIds() {
        VectorDocument doc = buildDoc("1", "doc1", 0, new float[]{1, 0, 0}, "测试文档");
        store.upsert("test", List.of(doc));
        store.deleteByIds("test", List.of("1"));
        List<VectorDocument> results = store.search("test", new float[]{1, 0, 0}, 2, 0.0);
        assertTrue(results.isEmpty());
    }

    private VectorDocument buildDoc(String id, String docId, int chunkIndex, float[] embedding, String content) {
        VectorDocument doc = new VectorDocument();
        doc.setId(id);
        doc.setCollection("test");
        doc.setDocId(docId);
        doc.setChunkIndex(chunkIndex);
        doc.setContent(content);
        doc.setEmbedding(embedding);
        return doc;
    }
}
