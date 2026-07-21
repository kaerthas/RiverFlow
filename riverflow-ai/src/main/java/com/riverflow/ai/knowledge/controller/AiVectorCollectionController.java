package com.riverflow.ai.knowledge.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.riverflow.ai.knowledge.entity.AiVectorCollection;
import com.riverflow.ai.knowledge.service.AiVectorCollectionService;
import com.riverflow.ai.knowledge.vector.MilvusVectorStore;
import com.riverflow.ai.knowledge.vector.VectorStoreProvider;
import com.riverflow.ai.knowledge.vector.VectorStoreProviderFactory;
import com.riverflow.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 向量库管理接口
 */
@Slf4j
@RestController
@RequestMapping("/ai/vector")
public class AiVectorCollectionController {

    private final AiVectorCollectionService collectionService;
    private final VectorStoreProviderFactory vectorStoreProviderFactory;
    private final MilvusVectorStore milvusVectorStore;

    @Autowired
    public AiVectorCollectionController(AiVectorCollectionService collectionService,
                                        VectorStoreProviderFactory vectorStoreProviderFactory,
                                        MilvusVectorStore milvusVectorStore) {
        this.collectionService = collectionService;
        this.vectorStoreProviderFactory = vectorStoreProviderFactory;
        this.milvusVectorStore = milvusVectorStore;
    }

    /**
     * 分页查询向量集合
     */
    @GetMapping("/collections")
    public R<Page<AiVectorCollection>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return R.ok(collectionService.page(page, size, keyword));
    }

    /**
     * 查询全部向量集合
     */
    @GetMapping("/collections/all")
    public R<List<AiVectorCollection>> listAll() {
        return R.ok(collectionService.list());
    }

    /**
     * 根据 ID 查询
     */
    @GetMapping("/collections/{id}")
    public R<AiVectorCollection> getById(@PathVariable Long id) {
        AiVectorCollection collection = collectionService.getById(id);
        if (collection == null) {
            return R.fail("集合不存在");
        }
        return R.ok(collection);
    }

    /**
     * 新增集合
     */
    @PostMapping("/collections")
    public R<Long> save(@RequestBody AiVectorCollection collection) {
        collectionService.save(collection);
        return R.ok(collection.getId());
    }

    /**
     * 更新集合
     */
    @PutMapping("/collections/{id}")
    public R<Void> update(@PathVariable Long id, @RequestBody AiVectorCollection collection) {
        collection.setId(id);
        collectionService.update(collection);
        return R.ok();
    }

    /**
     * 删除集合
     */
    @DeleteMapping("/collections/{id}")
    public R<Void> delete(@PathVariable Long id) {
        collectionService.delete(id);
        return R.ok();
    }

    /**
     * 设置默认集合
     */
    @PostMapping("/collections/{id}/default")
    public R<Void> setDefault(@PathVariable Long id) {
        collectionService.setDefault(id);
        return R.ok();
    }

    /**
     * 测试向量库连接
     */
    @PostMapping("/collections/{id}/test")
    public R<String> testConnection(@PathVariable Long id) {
        AiVectorCollection collection = collectionService.getById(id);
        if (collection == null) {
            return R.fail("集合不存在");
        }
        try {
            if (MilvusVectorStore.TYPE.equalsIgnoreCase(collection.getStoreType())) {
                boolean ok = milvusVectorStore.testConnection(collection);
                return ok ? R.ok("Milvus 连接成功: " + collection.getMilvusHost() + ":" + collection.getMilvusPort())
                        : R.fail("Milvus 连接失败");
            }
            VectorStoreProvider provider = vectorStoreProviderFactory.getProvider(collection.getStoreType());
            provider.createCollection(collection.getCollection(), collection.getDimension(),
                    com.riverflow.ai.knowledge.vector.DistanceMetric.valueOf(collection.getDistanceMetric()));
            boolean exists = provider.collectionExists(collection.getCollection());
            if (exists) {
                return R.ok("向量库连接成功: " + collection.getStoreType());
            }
            return R.fail("向量库连接失败: 无法创建或访问集合");
        } catch (Exception e) {
            log.error("向量库连接测试失败: id={}", id, e);
            return R.fail("向量库连接失败: " + e.getMessage());
        }
    }
}
