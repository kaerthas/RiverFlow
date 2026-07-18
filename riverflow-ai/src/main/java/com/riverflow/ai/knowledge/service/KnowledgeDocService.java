package com.riverflow.ai.knowledge.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.riverflow.ai.knowledge.dto.KnowledgeDocRequest;
import com.riverflow.ai.knowledge.entity.AiKnowledgeChunk;
import com.riverflow.ai.knowledge.entity.AiKnowledgeDoc;
import com.riverflow.ai.knowledge.mapper.AiKnowledgeChunkMapper;
import com.riverflow.ai.knowledge.mapper.AiKnowledgeDocMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 知识文档管理服务
 */
@Service
public class KnowledgeDocService {

    private final AiKnowledgeDocMapper docMapper;
    private final AiKnowledgeChunkMapper chunkMapper;

    @Autowired
    public KnowledgeDocService(AiKnowledgeDocMapper docMapper, AiKnowledgeChunkMapper chunkMapper) {
        this.docMapper = docMapper;
        this.chunkMapper = chunkMapper;
    }

    /**
     * 分页查询文档
     */
    public Page<AiKnowledgeDoc> pageDocs(int page, int size, String sourceType, String keyword) {
        LambdaQueryWrapper<AiKnowledgeDoc> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiKnowledgeDoc::getDelFlag, 0);
        if (StringUtils.hasText(sourceType)) {
            wrapper.eq(AiKnowledgeDoc::getSourceType, sourceType);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(AiKnowledgeDoc::getTitle, keyword)
                    .or()
                    .like(AiKnowledgeDoc::getContent, keyword));
        }
        wrapper.orderByDesc(AiKnowledgeDoc::getCreateTime);
        return docMapper.selectPage(new Page<>(page, size), wrapper);
    }

    /**
     * 根据 ID 查询文档
     */
    public AiKnowledgeDoc getById(Long id) {
        return docMapper.selectById(id);
    }

    /**
     * 查询文档分块
     */
    public List<AiKnowledgeChunk> getChunks(Long docId) {
        return chunkMapper.selectByDocId(docId);
    }

    /**
     * 将请求转换为实体
     */
    public AiKnowledgeDoc toEntity(KnowledgeDocRequest request) {
        AiKnowledgeDoc doc = new AiKnowledgeDoc();
        doc.setTitle(request.getTitle());
        doc.setSourceType(request.getSourceType());
        doc.setSourceId(request.getSourceId());
        doc.setContent(request.getContent());
        doc.setCollection(request.getCollection());
        doc.setEnabled(1);
        doc.setVectorStatus(0);
        return doc;
    }
}
