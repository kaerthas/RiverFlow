package com.riverflow.ai.knowledge.service;

import com.riverflow.ai.config.AiProperties;
import com.riverflow.ai.knowledge.embedding.EmbeddingClient;
import com.riverflow.ai.knowledge.embedding.EmbeddingClientFactory;
import com.riverflow.ai.knowledge.vector.DistanceMetric;
import com.riverflow.ai.knowledge.vector.InMemoryVectorStore;
import com.riverflow.ai.knowledge.service.VectorCollectionResolver;
import com.riverflow.ai.knowledge.entity.AiVectorCollection;
import com.riverflow.ai.knowledge.vector.VectorDocument;
import com.riverflow.ai.knowledge.vector.VectorStoreProviderFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * RAG 检索服务单元测试
 */
class KnowledgeRagServiceTest {

    private KnowledgeRagService knowledgeRagService;
    private EmbeddingClientFactory embeddingClientFactory;
    private VectorStoreProviderFactory vectorStoreProviderFactory;
    private InMemoryVectorStore vectorStore;
    private AiProperties aiProperties;

    private VectorCollectionResolver vectorCollectionResolver;

    @BeforeEach
    void setUp() {
        aiProperties = new AiProperties();
        aiProperties.getKnowledge().getRag().setEnabled(true);
        aiProperties.getKnowledge().getRag().setTopK(5);
        aiProperties.getKnowledge().getRag().setMinScore(0.5);
        aiProperties.getKnowledge().getRag().setCollection("test");
        aiProperties.getKnowledge().getVectorStore().setDefaultCollection("test");

        embeddingClientFactory = mock(EmbeddingClientFactory.class);
        vectorStore = new InMemoryVectorStore();
        vectorStore.createCollection("test", 3, DistanceMetric.COSINE);
        vectorStoreProviderFactory = mock(VectorStoreProviderFactory.class);
        when(vectorStoreProviderFactory.getProvider(anyString())).thenReturn(vectorStore);
        vectorCollectionResolver = mock(VectorCollectionResolver.class);

        EmbeddingClient embeddingClient = new EmbeddingClient() {
            @Override
            public List<float[]> embed(List<String> texts) {
                return Collections.singletonList(new float[]{1, 0, 0});
            }

            @Override
            public float[] embed(String text) {
                return new float[]{1, 0, 0};
            }

            @Override
            public int dimension() {
                return 3;
            }

            @Override
            public String name() {
                return "test";
            }
        };
        when(embeddingClientFactory.create(any(AiVectorCollection.class))).thenReturn(embeddingClient);

        knowledgeRagService = new KnowledgeRagService(aiProperties, embeddingClientFactory, vectorStoreProviderFactory, vectorCollectionResolver);
    }

    @Test
    void testSearchGrouped() {
        AiVectorCollection collection = new AiVectorCollection();
        collection.setCollection("test");
        collection.setStoreType("memory");
        collection.setDimension(3);
        collection.setDistanceMetric("COSINE");
        when(vectorCollectionResolver.resolve(any(), any())).thenReturn(collection);

        VectorDocument apiDoc = new VectorDocument();
        apiDoc.setId("api_1");
        apiDoc.setCollection("test");
        apiDoc.setDocId("1");
        apiDoc.setChunkIndex(0);
        apiDoc.setContent("接口文档内容");
        apiDoc.setEmbedding(new float[]{1, 0, 0});
        apiDoc.setMetadata(Map.of("sourceType", "api", "title", "测试接口"));

        VectorDocument flowDoc = new VectorDocument();
        flowDoc.setId("flow_1");
        flowDoc.setCollection("test");
        flowDoc.setDocId("2");
        flowDoc.setChunkIndex(0);
        flowDoc.setContent("流程文档内容");
        flowDoc.setEmbedding(new float[]{0, 1, 0});
        flowDoc.setMetadata(Map.of("sourceType", "flow", "title", "测试流程"));

        vectorStore.upsert("test", List.of(apiDoc, flowDoc));

        Map<String, List<VectorDocument>> grouped = knowledgeRagService.searchGrouped("查询", null, null, null);
        assertTrue(grouped.containsKey("api"), "实际分组: " + grouped.keySet());
        assertEquals(1, grouped.get("api").size());
        assertEquals("接口文档内容", grouped.get("api").get(0).getContent());
    }

    @Test
    void testRagDisabled() {
        aiProperties.getKnowledge().getRag().setEnabled(false);
        List<VectorDocument> results = knowledgeRagService.search("查询", null, null, null);
        assertTrue(results.isEmpty());
    }
}
