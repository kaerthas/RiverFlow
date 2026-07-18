package com.riverflow.ai.knowledge.service;

import com.riverflow.ai.knowledge.entity.ApiCatalog;
import com.riverflow.ai.knowledge.entity.Datasource;
import com.riverflow.ai.knowledge.entity.FlowDefinition;
import com.riverflow.ai.knowledge.mapper.AiKnowledgeMapper;
import com.riverflow.ai.knowledge.vector.VectorDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI 知识库检索服务
 *
 * <p>支持 MySQL LIKE 关键词检索（fallback）和向量语义检索。</p>
 */
@Service
public class AiKnowledgeService {

    private final AiKnowledgeMapper knowledgeMapper;
    private final KnowledgeRagService knowledgeRagService;

    @Autowired
    public AiKnowledgeService(AiKnowledgeMapper knowledgeMapper, KnowledgeRagService knowledgeRagService) {
        this.knowledgeMapper = knowledgeMapper;
        this.knowledgeRagService = knowledgeRagService;
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

    /**
     * 语义检索
     */
    public List<VectorDocument> searchSemantic(String query, String collection, Integer topK, Double minScore) {
        return knowledgeRagService.search(query, collection, topK, minScore);
    }

    /**
     * 按来源类型分组的语义检索
     */
    public Map<String, List<VectorDocument>> searchSemanticGrouped(String query, String collection, Integer topK, Double minScore) {
        return knowledgeRagService.searchGrouped(query, collection, topK, minScore);
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
