package com.riverflow.ai.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.riverflow.ai.knowledge.entity.AiVectorCollection;
import org.apache.ibatis.annotations.Mapper;

/**
 * 向量集合配置 Mapper
 */
@Mapper
public interface AiVectorCollectionMapper extends BaseMapper<AiVectorCollection> {
}
