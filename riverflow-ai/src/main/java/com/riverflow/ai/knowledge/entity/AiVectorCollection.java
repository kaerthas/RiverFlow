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
     * 描述
     */
    private String description;

    /**
     * 是否启用
     */
    private Integer enabled;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
