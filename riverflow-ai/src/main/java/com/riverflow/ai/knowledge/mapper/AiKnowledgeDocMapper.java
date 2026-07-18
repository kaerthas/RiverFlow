package com.riverflow.ai.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.riverflow.ai.knowledge.entity.AiKnowledgeDoc;
import org.apache.ibatis.annotations.Mapper;

/**
 * 知识文档 Mapper
 */
@Mapper
public interface AiKnowledgeDocMapper extends BaseMapper<AiKnowledgeDoc> {
}
