package com.riverflow.ai.prompt.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.riverflow.ai.prompt.entity.AiPrompt;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * AI Prompt 模板 Mapper
 */
@Mapper
public interface AiPromptMapper extends BaseMapper<AiPrompt> {

    /**
     * 查询启用的 Prompt，按 model 精确匹配优先，无则取 default
     */
    @Select("SELECT * FROM wf_ai_prompt WHERE scene = #{scene} AND version = #{version} " +
            "AND enabled = 1 AND del_flag = 0 AND model IN (#{model}, 'default') " +
            "ORDER BY FIELD(model, #{model}, 'default'), sort_no ASC LIMIT 1")
    AiPrompt findEnabledBySceneAndModelAndVersion(@Param("scene") String scene,
                                                   @Param("model") String model,
                                                   @Param("version") String version);

    /**
     * 查询某场景下所有启用的 Prompt
     */
    @Select("SELECT * FROM wf_ai_prompt WHERE scene = #{scene} AND enabled = 1 AND del_flag = 0 " +
            "ORDER BY model, version, sort_no ASC")
    List<AiPrompt> findEnabledByScene(@Param("scene") String scene);
}
