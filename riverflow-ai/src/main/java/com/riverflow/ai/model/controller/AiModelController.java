package com.riverflow.ai.model.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.riverflow.ai.config.AiProperties;
import com.riverflow.ai.model.entity.AiModel;
import com.riverflow.ai.model.service.AiModelService;
import com.riverflow.ai.provider.AiProviderLoader;
import com.riverflow.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI 模型配置管理
 *
 * <p>提供 LLM Provider/模型的增删改查，以及运行时刷新能力。
 * 前端通过 riverflow-admin 的 /ai/** 代理访问。
 */
@Slf4j
@RestController
@RequestMapping("/ai/model")
public class AiModelController {

    private final AiModelService aiModelService;
    private final AiProviderLoader aiProviderLoader;
    private final AiProperties aiProperties;

    @Autowired
    public AiModelController(AiModelService aiModelService,
                             AiProviderLoader aiProviderLoader,
                             AiProperties aiProperties) {
        this.aiModelService = aiModelService;
        this.aiProviderLoader = aiProviderLoader;
        this.aiProperties = aiProperties;
    }

    /**
     * 分页列表
     */
    @GetMapping("/list")
    public R<Page<AiModel>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String providerType) {
        Page<AiModel> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<AiModel> qw = new LambdaQueryWrapper<>();
        qw.eq(AiModel::getDelFlag, 0);
        if (StringUtils.hasText(keyword)) {
            qw.and(w -> w.like(AiModel::getModelCode, keyword)
                    .or()
                    .like(AiModel::getModelName, keyword)
                    .or()
                    .like(AiModel::getProviderName, keyword));
        }
        if (StringUtils.hasText(providerType)) {
            qw.eq(AiModel::getProviderType, providerType);
        }
        qw.orderByAsc(AiModel::getSortNo).orderByDesc(AiModel::getCreateTime);
        Page<AiModel> result = aiModelService.page(pageParam, qw);
        result.getRecords().forEach(this::maskApiKey);
        return R.ok(result);
    }

    /**
     * 详情
     */
    @GetMapping("/{id}")
    public R<AiModel> getById(@PathVariable Long id) {
        AiModel model = aiModelService.getById(id);
        if (model != null) {
            maskApiKey(model);
        }
        return R.ok(model);
    }

    /**
     * 新增
     */
    @PostMapping
    public R<Long> save(@RequestBody AiModel model) {
        normalize(model);
        if (existCode(model.getModelCode(), null)) {
            return R.fail("模型编码已存在");
        }
        aiModelService.save(model);
        refreshProviders();
        return R.ok(model.getId());
    }

    /**
     * 修改
     */
    @PutMapping
    public R<Long> update(@RequestBody AiModel model) {
        if (model.getId() == null) {
            return R.fail("ID 不能为空");
        }
        normalize(model);
        if (existCode(model.getModelCode(), model.getId())) {
            return R.fail("模型编码已存在");
        }
        // 前端传过来的 apiKey 可能是脱敏后的占位符，需要判断
        AiModel existing = aiModelService.getById(model.getId());
        if (existing != null && isMasked(model.getApiKey())) {
            model.setApiKey(existing.getApiKey());
        }
        aiModelService.updateById(model);
        refreshProviders();
        return R.ok(model.getId());
    }

    /**
     * 删除（逻辑删除）
     */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        aiModelService.removeById(id);
        refreshProviders();
        return R.ok();
    }

    /**
     * 刷新运行时 Provider
     */
    @PostMapping("/reload")
    public R<Void> reload() {
        refreshProviders();
        return R.ok();
    }

    /**
     * 下拉选项（给 AI 助手页面使用）
     */
    @GetMapping("/options")
    public R<List<AiModel>> options() {
        LambdaQueryWrapper<AiModel> qw = new LambdaQueryWrapper<>();
        qw.eq(AiModel::getDelFlag, 0)
                .eq(AiModel::getStatus, 1)
                .orderByAsc(AiModel::getSortNo)
                .orderByDesc(AiModel::getCreateTime);
        List<AiModel> list = aiModelService.list(qw);
        list.forEach(this::maskApiKey);
        return R.ok(list);
    }

    private boolean existCode(String code, Long excludeId) {
        QueryWrapper<AiModel> qw = new QueryWrapper<>();
        qw.eq("model_code", code).eq("del_flag", 0);
        if (excludeId != null) {
            qw.ne("id", excludeId);
        }
        return aiModelService.count(qw) > 0;
    }

    private void normalize(AiModel model) {
        if (StringUtils.hasText(model.getModelCode())) {
            model.setModelCode(model.getModelCode().trim().toLowerCase());
        }
        if (StringUtils.hasText(model.getModelName())) {
            model.setModelName(model.getModelName().trim());
        }
        if (StringUtils.hasText(model.getProviderType())) {
            model.setProviderType(model.getProviderType().trim().toLowerCase());
        }
        if (StringUtils.hasText(model.getProviderName())) {
            model.setProviderName(model.getProviderName().trim());
        }
        if (StringUtils.hasText(model.getBaseUrl())) {
            model.setBaseUrl(model.getBaseUrl().trim());
        }
        if (StringUtils.hasText(model.getApiKey())) {
            model.setApiKey(model.getApiKey().trim());
        }
        if (model.getTemperature() == null) {
            model.setTemperature(0.2f);
        }
        if (model.getMaxTokens() == null) {
            model.setMaxTokens(4096);
        }
        if (model.getTimeout() == null) {
            model.setTimeout(30000);
        }
        if (model.getIsDefault() == null) {
            model.setIsDefault(0);
        }
        if (model.getStatus() == null) {
            model.setStatus(1);
        }
        if (model.getSortNo() == null) {
            model.setSortNo(0);
        }
        if (!StringUtils.hasText(model.getModelCode()) && StringUtils.hasText(model.getModelName())) {
            model.setModelCode(model.getModelName().trim().toLowerCase().replaceAll("\\s+", "-"));
        }
    }

    private boolean isMasked(String apiKey) {
        return apiKey != null && apiKey.contains("*");
    }

    private void maskApiKey(AiModel model) {
        String key = model.getApiKey();
        if (!StringUtils.hasText(key)) {
            return;
        }
        if (key.length() <= 8) {
            model.setApiKey("********");
        } else {
            model.setApiKey(key.substring(0, 4) + "****" + key.substring(key.length() - 4));
        }
    }

    private void refreshProviders() {
        try {
            aiProviderLoader.loadFromDatabase(aiModelService, aiProperties);
        } catch (Exception e) {
            log.error("刷新 AI Provider 失败", e);
        }
    }
}
