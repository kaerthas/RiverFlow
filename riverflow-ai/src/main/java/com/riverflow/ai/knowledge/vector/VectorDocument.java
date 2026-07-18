package com.riverflow.ai.knowledge.vector;

import lombok.Data;

import java.util.Map;

/**
 * 向量文档
 *
 * <p>表示一个已分块并（或）已向量化的知识片段。</p>
 */
@Data
public class VectorDocument {

    /**
     * 唯一 ID：docId_chunkIndex
     */
    private String id;

    /**
     * 所属集合/表
     */
    private String collection;

    /**
     * 业务文档 ID
     */
    private String docId;

    /**
     * 分块序号
     */
    private int chunkIndex;

    /**
     * 文本内容
     */
    private String content;

    /**
     * 向量，允许为空（部分 Store 在 upsert 时自行计算）
     */
    private float[] embedding;

    /**
     * 元数据：sourceType、sourceId、title 等
     */
    private Map<String, Object> metadata;

    /**
     * 相似度得分（检索结果回填）
     */
    private Double score;
}
