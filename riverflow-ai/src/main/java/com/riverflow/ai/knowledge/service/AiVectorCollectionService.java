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
        Page<AiVectorCollection> result = collectionMapper.selectPage(new Page<>(page, size), wrapper);
        fillDocCount(result.getRecords());
        return result;
    }

    /**
     * 列表查询
     */
    public List<AiVectorCollection> list() {
        List<AiVectorCollection> records = collectionMapper.selectList(null);
        fillDocCount(records);
        return records;
    }

    /**
     * 根据 ID 查询
     */
    public AiVectorCollection getById(Long id) {
        AiVectorCollection collection = collectionMapper.selectById(id);
        if (collection != null) {
            collection.setDocCount(countDocs(id, collection.getCollection()));
        }
        return collection;
    }

    private void fillDocCount(List<AiVectorCollection> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        for (AiVectorCollection record : records) {
            record.setDocCount(countDocs(record.getId(), record.getCollection()));
        }
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
        if (collection.getIsDefault() == null) {
            collection.setIsDefault(0);
        }
        collectionMapper.insert(collection);
        // 如果设为默认，需要把其他集合设为非默认
        if (Integer.valueOf(1).equals(collection.getIsDefault())) {
            clearOtherDefault(collection.getId());
        }
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

        AiVectorCollection existing = collectionMapper.selectById(collection.getId());
        if (existing == null) {
            throw new RuntimeException("集合不存在");
        }
        // 防止前端未传字段时被覆盖为 null
        if (!StringUtils.hasText(collection.getEmbeddingBaseUrl())) {
            collection.setEmbeddingBaseUrl(existing.getEmbeddingBaseUrl());
        }
        if (!StringUtils.hasText(collection.getEmbeddingApiKey())) {
            collection.setEmbeddingApiKey(existing.getEmbeddingApiKey());
        }
        if (!StringUtils.hasText(collection.getEmbeddingModel())) {
            collection.setEmbeddingModel(existing.getEmbeddingModel());
        }
        if (collection.getIsDefault() == null) {
            collection.setIsDefault(existing.getIsDefault());
        }
        if (!StringUtils.hasText(collection.getMilvusHost())) {
            collection.setMilvusHost(existing.getMilvusHost());
        }
        if (collection.getMilvusPort() == null) {
            collection.setMilvusPort(existing.getMilvusPort());
        }
        if (!StringUtils.hasText(collection.getMilvusDatabase())) {
            collection.setMilvusDatabase(existing.getMilvusDatabase());
        }
        if (!StringUtils.hasText(collection.getMilvusToken())) {
            collection.setMilvusToken(existing.getMilvusToken());
        }
        if (collection.getMilvusSecure() == null) {
            collection.setMilvusSecure(existing.getMilvusSecure());
        }

        collection.setUpdateTime(LocalDateTime.now());
        collectionMapper.updateById(collection);
        // 如果设为默认，需要把其他集合设为非默认
        if (Integer.valueOf(1).equals(collection.getIsDefault())) {
            clearOtherDefault(collection.getId());
        }
    }

    /**
     * 设置默认集合
     */
    public void setDefault(Long id) {
        AiVectorCollection collection = collectionMapper.selectById(id);
        if (collection == null) {
            throw new RuntimeException("集合不存在");
        }
        collection.setIsDefault(1);
        collection.setUpdateTime(LocalDateTime.now());
        collectionMapper.updateById(collection);
        clearOtherDefault(id);
    }

    private void clearOtherDefault(Long excludeId) {
        LambdaQueryWrapper<AiVectorCollection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiVectorCollection::getIsDefault, 1);
        if (excludeId != null) {
            wrapper.ne(AiVectorCollection::getId, excludeId);
        }
        List<AiVectorCollection> others = collectionMapper.selectList(wrapper);
        for (AiVectorCollection other : others) {
            other.setIsDefault(0);
            other.setUpdateTime(LocalDateTime.now());
            collectionMapper.updateById(other);
        }
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
        wrapper.eq(AiKnowledgeDoc::getCollectionId, id)
                .or()
                .eq(AiKnowledgeDoc::getCollection, collection.getCollection());
        long count = docMapper.selectCount(wrapper);
        if (count > 0) {
            throw new RuntimeException("该集合下存在 " + count + " 个文档，无法删除");
        }
        collectionMapper.deleteById(id);
    }

    /**
     * 统计集合下关联文档数
     */
    public long countDocs(Long collectionId, String collectionName) {
        LambdaQueryWrapper<AiKnowledgeDoc> wrapper = new LambdaQueryWrapper<>();
        if (collectionId != null) {
            wrapper.eq(AiKnowledgeDoc::getCollectionId, collectionId);
        } else if (StringUtils.hasText(collectionName)) {
            wrapper.eq(AiKnowledgeDoc::getCollection, collectionName);
        } else {
            return 0;
        }
        return docMapper.selectCount(wrapper);
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
