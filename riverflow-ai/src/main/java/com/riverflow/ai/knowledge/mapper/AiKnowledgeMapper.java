package com.riverflow.ai.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.riverflow.ai.knowledge.entity.ApiCatalog;
import com.riverflow.ai.knowledge.entity.ApiParam;
import com.riverflow.ai.knowledge.entity.Datasource;
import com.riverflow.ai.knowledge.entity.FlowDefinition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * AI 知识库检索 Mapper
 */
@Mapper
public interface AiKnowledgeMapper extends BaseMapper<ApiCatalog> {

    List<ApiCatalog> searchApis(@Param("keywords") List<String> keywords, @Param("limit") int limit);

    List<ApiParam> searchApiParamsByApiIds(@Param("apiIds") List<Long> apiIds);

    List<FlowDefinition> searchFlows(@Param("keywords") List<String> keywords, @Param("limit") int limit);

    List<Datasource> searchDatasources(@Param("keywords") List<String> keywords, @Param("limit") int limit);
}
