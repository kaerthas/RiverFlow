package com.riverflow.ai.knowledge.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.riverflow.ai.knowledge.dto.KnowledgeDocRequest;
import com.riverflow.ai.knowledge.dto.KnowledgeRebuildRequest;
import com.riverflow.ai.knowledge.dto.KnowledgeSearchRequest;
import com.riverflow.ai.knowledge.entity.AiKnowledgeChunk;
import com.riverflow.ai.knowledge.entity.AiKnowledgeDoc;
import com.riverflow.ai.knowledge.entity.AiVectorCollection;
import com.riverflow.ai.knowledge.mapper.AiVectorCollectionMapper;
import com.riverflow.ai.knowledge.service.KnowledgeDocService;
import com.riverflow.ai.knowledge.service.KnowledgeIndexingService;
import com.riverflow.ai.knowledge.service.KnowledgeRagService;
import com.riverflow.ai.knowledge.vector.VectorDocument;
import com.riverflow.common.result.R;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * AI 知识库管理接口
 */
@RestController
@RequestMapping("/ai/knowledge")
public class AiKnowledgeController {

    private final KnowledgeDocService knowledgeDocService;
    private final KnowledgeIndexingService knowledgeIndexingService;
    private final KnowledgeRagService knowledgeRagService;
    private final AiVectorCollectionMapper vectorCollectionMapper;

    @Autowired
    public AiKnowledgeController(KnowledgeDocService knowledgeDocService,
                                  KnowledgeIndexingService knowledgeIndexingService,
                                  KnowledgeRagService knowledgeRagService,
                                  AiVectorCollectionMapper vectorCollectionMapper) {
        this.knowledgeDocService = knowledgeDocService;
        this.knowledgeIndexingService = knowledgeIndexingService;
        this.knowledgeRagService = knowledgeRagService;
        this.vectorCollectionMapper = vectorCollectionMapper;
    }

    /**
     * 新增/上传文档并建立索引
     */
    @PostMapping("/docs")
    public R<Long> createDoc(@Valid @RequestBody KnowledgeDocRequest request) {
        AiKnowledgeDoc doc = knowledgeDocService.toEntity(request);
        knowledgeIndexingService.indexDoc(doc);
        return R.ok(doc.getId());
    }

    /**
     * 分页查询文档
     */
    @GetMapping("/docs")
    public R<Page<AiKnowledgeDoc>> listDocs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sourceType,
            @RequestParam(required = false) String keyword) {
        return R.ok(knowledgeDocService.pageDocs(page, size, sourceType, keyword));
    }

    /**
     * 删除文档
     */
    @DeleteMapping("/docs/{id}")
    public R<Void> deleteDoc(@PathVariable Long id) {
        knowledgeIndexingService.deleteDoc(id);
        return R.ok();
    }

    /**
     * 查询文档分块
     */
    @GetMapping("/docs/{id}/chunks")
    public R<List<AiKnowledgeChunk>> getChunks(@PathVariable Long id) {
        return R.ok(knowledgeDocService.getChunks(id));
    }

    /**
     * 重建索引
     */
    @PostMapping("/rebuild")
    public R<Void> rebuild(@RequestBody KnowledgeRebuildRequest request) {
        knowledgeIndexingService.rebuildCollection(request != null ? request.getCollection() : null);
        return R.ok();
    }

    /**
     * 语义检索测试
     */
    @PostMapping("/search")
    public R<List<VectorDocument>> search(@Valid @RequestBody KnowledgeSearchRequest request) {
        return R.ok(knowledgeRagService.search(request.getQuery(), request.getCollection(),
                request.getTopK(), request.getMinScore()));
    }

    /**
     * 语义检索测试（按来源分组）
     */
    @PostMapping("/search/grouped")
    public R<Map<String, List<VectorDocument>>> searchGrouped(@Valid @RequestBody KnowledgeSearchRequest request) {
        return R.ok(knowledgeRagService.searchGrouped(request.getQuery(), request.getCollection(),
                request.getTopK(), request.getMinScore()));
    }

    /**
     * 查询向量集合配置
     */
    @GetMapping("/collections")
    public R<List<AiVectorCollection>> listCollections() {
        return R.ok(vectorCollectionMapper.selectList(null));
    }
}
