package com.riverflow.ai.knowledge.vector;

import com.riverflow.ai.config.AiProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * VectorStoreProvider 工厂
 *
 * <p>根据配置选择对应的向量存储实现。</p>
 */
@Slf4j
@Component
public class VectorStoreProviderFactory {

    private final AiProperties aiProperties;
    private final InMemoryVectorStore inMemoryVectorStore;
    private final MilvusVectorStore milvusVectorStore;
    private final PgVectorStore pgVectorStore;

    @Autowired
    public VectorStoreProviderFactory(AiProperties aiProperties,
                                       InMemoryVectorStore inMemoryVectorStore,
                                       MilvusVectorStore milvusVectorStore,
                                       PgVectorStore pgVectorStore) {
        this.aiProperties = aiProperties;
        this.inMemoryVectorStore = inMemoryVectorStore;
        this.milvusVectorStore = milvusVectorStore;
        this.pgVectorStore = pgVectorStore;
    }

    /**
     * 获取默认 VectorStoreProvider
     */
    public VectorStoreProvider getProvider() {
        String type = aiProperties.getKnowledge().getVectorStore().getType();
        return getProvider(type);
    }

    /**
     * 按类型获取 VectorStoreProvider
     */
    public VectorStoreProvider getProvider(String type) {
        if (type == null) {
            type = "milvus";
        }
        return switch (type.trim().toLowerCase()) {
            case MilvusVectorStore.TYPE -> milvusVectorStore;
            case PgVectorStore.TYPE -> pgVectorStore;
            case InMemoryVectorStore.TYPE -> inMemoryVectorStore;
            default -> throw new IllegalArgumentException("不支持的向量库类型: " + type);
        };
    }
}
