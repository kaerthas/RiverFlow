package com.riverflow.ai.knowledge.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.riverflow.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI 知识文档元数据
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wf_ai_knowledge_doc")
public class AiKnowledgeDoc extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 文档标题
     */
    private String title;

    /**
     * 来源类型：flow / api / datasource / dynamic_table / file / upload
     */
    private String sourceType;

    /**
     * 来源业务ID
     */
    private String sourceId;

    /**
     * 原始内容
     */
    private String content;

    /**
     * 分块数量
     */
    private Integer chunkCount;

    /**
     * 向量状态：0-未索引 1-索引中 2-已索引 3-失败
     */
    private Integer vectorStatus;

    /**
     * 所属向量集合配置ID
     */
    private Long collectionId;

    /**
     * 所属向量集合（冗余名称）
     */
    private String collection;

    /**
     * 是否启用：0-停用 1-启用
     */
    private Integer enabled;
}
