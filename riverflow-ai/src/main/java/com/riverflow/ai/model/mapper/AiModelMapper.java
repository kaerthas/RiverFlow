package com.riverflow.ai.model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.riverflow.ai.model.entity.AiModel;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 模型配置 Mapper
 */
@Mapper
public interface AiModelMapper extends BaseMapper<AiModel> {
}
