package com.riverflow.ai.knowledge.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 重建索引请求
 */
@Data
public class KnowledgeRebuildRequest {

    /**
     * 指定重建的集合配置ID，为空重建默认集合
     */
    private Long collectionId;

    /**
     * 指定重建的集合名称（兼容字段）
     */
    private String collection;
}
