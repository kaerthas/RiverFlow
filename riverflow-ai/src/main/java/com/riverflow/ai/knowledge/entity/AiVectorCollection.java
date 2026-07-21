package com.riverflow.ai.knowledge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 向量集合配置
 */
@Data
@TableName("wf_ai_vector_collection")
public class AiVectorCollection {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 集合/表名称
     */
    private String collection;

    /**
     * 向量库类型：milvus / pgvector / memory
     */
    private String storeType;

    /**
     * 向量维度
     */
    private Integer dimension;

    /**
     * 距离度量：COSINE / IP / L2
     */
    private String distanceMetric;

    /**
     * Embedding 类型/客户端
     */
    private String embeddingType;

    /**
     * Embedding 基础 URL（为空则使用全局配置）
     */
    private String embeddingBaseUrl;

    /**
     * Embedding API Key（为空则使用全局配置）
     */
    private String embeddingApiKey;

    /**
     * Embedding 模型（为空则使用全局配置）
     */
    private String embeddingModel;

    /**
     * Milvus 主机地址（为空则使用全局配置）
     */
    private String milvusHost;

    /**
     * Milvus 端口（为空则使用全局配置）
     */
    private Integer milvusPort;

    /**
     * Milvus 数据库名（为空则使用全局配置）
     */
    private String milvusDatabase;

    /**
     * Milvus Token（为空则使用全局配置）
     */
    private String milvusToken;

    /**
     * 是否使用 TLS/HTTPS（为空则使用全局配置）
     */
    private Integer milvusSecure;

    /**
     * 描述
     */
    private String description;

    /**
     * 是否启用
     */
    private Integer enabled;

    /**
     * 是否默认集合：0-否 1-是
     */
    private Integer isDefault;

    /**
     * 关联文档数（非持久化，用于前端展示）
     */
    private transient Long docCount;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
