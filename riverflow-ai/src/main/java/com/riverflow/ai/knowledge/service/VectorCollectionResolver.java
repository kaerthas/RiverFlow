package com.riverflow.ai.knowledge.service;

import com.riverflow.ai.config.AiProperties;
import com.riverflow.ai.knowledge.entity.AiVectorCollection;
import com.riverflow.ai.knowledge.mapper.AiVectorCollectionMapper;
import com.riverflow.ai.knowledge.vector.DistanceMetric;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 向量集合配置解析器
 *
 * <p>将知识文档的 collectionId / collection 转换为统一的 AiVectorCollection 配置。</p>
 */
@Component
public class VectorCollectionResolver {

    private static final Long DEFAULT_COLLECTION_ID = 1L;

    private final AiVectorCollectionMapper collectionMapper;
    private final AiProperties aiProperties;

    @Autowired
    public VectorCollectionResolver(AiVectorCollectionMapper collectionMapper, AiProperties aiProperties) {
        this.collectionMapper = collectionMapper;
        this.aiProperties = aiProperties;
    }

    /**
     * 解析向量集合配置
     *
     * @param collectionId   集合配置ID，优先使用
     * @param collectionName 集合名称，兼容字段
     * @return 非空的集合配置
     */
    public AiVectorCollection resolve(Long collectionId, String collectionName) {
        AiVectorCollection config = null;
        if (collectionId != null) {
            config = collectionMapper.selectById(collectionId);
        }
        if (config == null && StringUtils.hasText(collectionName)) {
            config = collectionMapper.selectByCollection(collectionName);
        }
        if (config == null) {
            config = selectDefaultCollection();
        }
        if (config == null) {
            config = buildDefaultFromProperties();
        }
        return config;
    }

    /**
     * 根据集合名称解析
     */
    public AiVectorCollection resolve(String collectionName) {
        return resolve(null, collectionName);
    }

    /**
     * 查询数据库中默认集合
     */
    public AiVectorCollection selectDefaultCollection() {
        return collectionMapper.selectDefaultCollection();
    }

    /**
     * 将字符串距离度量转换为枚举
     */
    public DistanceMetric toDistanceMetric(String metric) {
        if (!StringUtils.hasText(metric)) {
            return DistanceMetric.COSINE;
        }
        try {
            return DistanceMetric.valueOf(metric.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return DistanceMetric.COSINE;
        }
    }

    private AiVectorCollection buildDefaultFromProperties() {
        AiProperties.VectorStoreConfig vectorStore = aiProperties.getKnowledge().getVectorStore();
        AiProperties.EmbeddingConfig embedding = aiProperties.getKnowledge().getEmbedding();
        AiVectorCollection config = new AiVectorCollection();
        config.setId(DEFAULT_COLLECTION_ID);
        config.setCollection(vectorStore.getDefaultCollection());
        config.setStoreType(vectorStore.getType());
        config.setDimension(embedding.getDimension());
        config.setDistanceMetric("COSINE");
        config.setEmbeddingType(embedding.getType());
        config.setEnabled(1);
        return config;
    }
}
