package com.riverflow.ai.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.riverflow.ai.knowledge.entity.AiKnowledgeChunk;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 知识文档分块 Mapper
 */
@Mapper
public interface AiKnowledgeChunkMapper extends BaseMapper<AiKnowledgeChunk> {

    /**
     * 按文档 ID 删除分块
     */
    int deleteByDocId(@Param("docId") Long docId);

    /**
     * 按文档 ID 查询分块
     */
    List<AiKnowledgeChunk> selectByDocId(@Param("docId") Long docId);
}
