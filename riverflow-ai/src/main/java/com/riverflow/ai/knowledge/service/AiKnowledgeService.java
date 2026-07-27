package com.riverflow.ai.knowledge.service;

import com.riverflow.ai.knowledge.entity.ApiCatalog;
import com.riverflow.ai.knowledge.entity.ApiParam;
import com.riverflow.ai.knowledge.entity.Datasource;
import com.riverflow.ai.knowledge.entity.FlowDefinition;
import com.riverflow.ai.knowledge.mapper.AiKnowledgeMapper;
import com.riverflow.ai.knowledge.vector.VectorDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

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
        return searchApisWithParams(query, limit);
    }

    /**
     * 检索 API 并一次性填充接口参数（header/query/body/response）。
     */
    public List<ApiCatalog> searchApisWithParams(String query, int limit) {
        List<ApiCatalog> apis = knowledgeMapper.searchApis(splitKeywords(query), limit);
        if (CollectionUtils.isEmpty(apis)) {
            return apis;
        }
        List<Long> apiIds = apis.stream().map(ApiCatalog::getId).distinct().collect(Collectors.toList());
        List<ApiParam> params = knowledgeMapper.searchApiParamsByApiIds(apiIds);
        if (CollectionUtils.isEmpty(params)) {
            return apis;
        }
        Map<Long, List<ApiParam>> paramMap = params.stream()
                .collect(Collectors.groupingBy(ApiParam::getApiId));
        for (ApiCatalog api : apis) {
            api.setParams(paramMap.getOrDefault(api.getId(), Collections.emptyList()));
        }
        return apis;
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
    public List<VectorDocument> searchSemantic(String query, Long collectionId, String collection, Integer topK, Double minScore) {
        return knowledgeRagService.search(query, collectionId, collection, topK, minScore);
    }

    /**
     * 按来源类型分组的语义检索
     */
    public Map<String, List<VectorDocument>> searchSemanticGrouped(String query, Long collectionId, String collection, Integer topK, Double minScore) {
        return knowledgeRagService.searchGrouped(query, collectionId, collection, topK, minScore);
    }

    /**
     * 语义检索（兼容旧接口）
     */
    public List<VectorDocument> searchSemantic(String query, String collection, Integer topK, Double minScore) {
        return knowledgeRagService.search(query, collection, topK, minScore);
    }

    /**
     * 按来源类型分组的语义检索（兼容旧接口）
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
