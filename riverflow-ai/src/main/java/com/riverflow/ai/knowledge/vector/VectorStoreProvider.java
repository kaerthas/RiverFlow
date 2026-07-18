package com.riverflow.ai.knowledge.vector;

import java.util.List;

/**
 * 向量存储 Provider 抽象
 *
 * <p>支持 Milvus、PGVector、InMemory 等多种实现。</p>
 */
public interface VectorStoreProvider {

    /**
     * Provider 类型：milvus / pgvector / memory
     */
    String type();

    /**
     * 创建集合/表
     */
    void createCollection(String collection, int dimension, DistanceMetric metric);

    /**
     * 批量插入或更新向量文档
     */
    void upsert(String collection, List<VectorDocument> documents);

    /**
     * 向量相似度检索
     */
    List<VectorDocument> search(String collection, float[] vector, int topK, double minScore);

    /**
     * 按 ID 删除
     */
    void deleteByIds(String collection, List<String> ids);

    /**
     * 删除集合/表
     */
    void dropCollection(String collection);

    /**
     * 判断集合/表是否存在
     */
    boolean collectionExists(String collection);
}
