package com.riverflow.ai.knowledge.service;

import com.riverflow.ai.knowledge.entity.ApiCatalog;
import com.riverflow.ai.knowledge.entity.Datasource;
import com.riverflow.ai.knowledge.entity.FlowDefinition;
import com.riverflow.ai.knowledge.mapper.AiKnowledgeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AI 知识库检索服务
 *
 * <p>短期基于 MySQL LIKE 匹配，从现有接口目录、流程定义、数据源中检索相关内容。
 */
@Service
public class AiKnowledgeService {

    private final AiKnowledgeMapper knowledgeMapper;

    @Autowired
    public AiKnowledgeService(AiKnowledgeMapper knowledgeMapper) {
        this.knowledgeMapper = knowledgeMapper;
    }

    public List<ApiCatalog> searchApis(String query, int limit) {
        return knowledgeMapper.searchApis(splitKeywords(query), limit);
    }

    public List<FlowDefinition> searchFlows(String query, int limit) {
        return knowledgeMapper.searchFlows(splitKeywords(query), limit);
    }

    public List<Datasource> searchDatasources(String query, int limit) {
        return knowledgeMapper.searchDatasources(splitKeywords(query), limit);
    }

    private List<String> splitKeywords(String query) {
        if (query == null || query.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(query.split("[\\s,，；;]+"))
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList());
    }
}
