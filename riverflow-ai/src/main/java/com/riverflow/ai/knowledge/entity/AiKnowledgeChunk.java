package com.riverflow.ai.knowledge.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 知识文档分块
 */
@Data
@TableName("wf_ai_knowledge_chunk")
public class AiKnowledgeChunk {

    private Long id;

    /**
     * 文档ID
     */
    private Long docId;

    /**
     * 分块序号
     */
    private Integer chunkIndex;

    /**
     * 分块内容
     */
    private String content;

    /**
     * 预估 token 长度
     */
    private Integer tokenLength;

    /**
     * 分块元数据（JSON）
     */
    private String metadata;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
