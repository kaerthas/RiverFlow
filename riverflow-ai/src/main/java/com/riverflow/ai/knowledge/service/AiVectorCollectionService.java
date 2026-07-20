package com.riverflow.ai.knowledge.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.riverflow.ai.knowledge.entity.AiKnowledgeDoc;
import com.riverflow.ai.knowledge.entity.AiVectorCollection;
import com.riverflow.ai.knowledge.mapper.AiKnowledgeDocMapper;
import com.riverflow.ai.knowledge.mapper.AiVectorCollectionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 向量集合配置服务
 */
@Service
public class AiVectorCollectionService {

    private final AiVectorCollectionMapper collectionMapper;
    private final AiKnowledgeDocMapper docMapper;

    @Autowired
    public AiVectorCollectionService(AiVectorCollectionMapper collectionMapper, AiKnowledgeDocMapper docMapper) {
        this.collectionMapper = collectionMapper;
        this.docMapper = docMapper;
    }

    /**
     * 分页查询
     */
    public Page<AiVectorCollection> page(int page, int size, String keyword) {
        LambdaQueryWrapper<AiVectorCollection> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(AiVectorCollection::getCollection, keyword)
                    .or()
                    .like(AiVectorCollection::getDescription, keyword);
        }
        wrapper.orderByDesc(AiVectorCollection::getCreateTime);
        return collectionMapper.selectPage(new Page<>(page, size), wrapper);
    }

    /**
     * 列表查询
     */
    public List<AiVectorCollection> list() {
        return collectionMapper.selectList(null);
    }

    /**
     * 根据 ID 查询
     */
    public AiVectorCollection getById(Long id) {
        return collectionMapper.selectById(id);
    }

    /**
     * 新增
     */
    public void save(AiVectorCollection collection) {
        validate(collection);
        checkDuplicate(collection, null);
        LocalDateTime now = LocalDateTime.now();
        collection.setCreateTime(now);
        collection.setUpdateTime(now);
        if (collection.getEnabled() == null) {
            collection.setEnabled(1);
        }
        collectionMapper.insert(collection);
    }

    /**
     * 更新
     */
    public void update(AiVectorCollection collection) {
        if (collection.getId() == null) {
            throw new IllegalArgumentException("ID 不能为空");
        }
        validate(collection);
        checkDuplicate(collection, collection.getId());
        collection.setUpdateTime(LocalDateTime.now());
        collectionMapper.updateById(collection);
    }

    /**
     * 删除
     */
    public void delete(Long id) {
        AiVectorCollection collection = collectionMapper.selectById(id);
        if (collection == null) {
            return;
        }
        // 检查是否有关联文档
        LambdaQueryWrapper<AiKnowledgeDoc> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiKnowledgeDoc::getCollection, collection.getCollection());
        long count = docMapper.selectCount(wrapper);
        if (count > 0) {
            throw new RuntimeException("该集合下存在 " + count + " 个文档，无法删除");
        }
        collectionMapper.deleteById(id);
    }

    private void validate(AiVectorCollection collection) {
        if (!StringUtils.hasText(collection.getCollection())) {
            throw new IllegalArgumentException("集合名称不能为空");
        }
        if (collection.getDimension() == null || collection.getDimension() <= 0) {
            throw new IllegalArgumentException("维度必须大于 0");
        }
        if (!StringUtils.hasText(collection.getStoreType())) {
            throw new IllegalArgumentException("向量库类型不能为空");
        }
        if (!StringUtils.hasText(collection.getEmbeddingType())) {
            throw new IllegalArgumentException("Embedding 类型不能为空");
        }
    }

    private void checkDuplicate(AiVectorCollection collection, Long excludeId) {
        LambdaQueryWrapper<AiVectorCollection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiVectorCollection::getCollection, collection.getCollection());
        if (excludeId != null) {
            wrapper.ne(AiVectorCollection::getId, excludeId);
        }
        if (collectionMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("集合名称已存在: " + collection.getCollection());
        }
    }
}
