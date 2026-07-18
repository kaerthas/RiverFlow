package com.riverflow.ai.prompt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.riverflow.ai.prompt.PromptTemplateLoader;
import com.riverflow.ai.prompt.entity.AiPrompt;
import com.riverflow.ai.prompt.service.AiPromptService;
import com.riverflow.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI Prompt 模板管理
 *
 * <p>提供 Prompt 的增删改查，以及运行时刷新能力。</p>
 */
@Slf4j
@RestController
@RequestMapping("/ai/prompt")
public class AiPromptController {

    private final AiPromptService aiPromptService;
    private final PromptTemplateLoader promptTemplateLoader;

    @Autowired
    public AiPromptController(AiPromptService aiPromptService,
                              PromptTemplateLoader promptTemplateLoader) {
        this.aiPromptService = aiPromptService;
        this.promptTemplateLoader = promptTemplateLoader;
    }

    /**
     * 分页列表
     */
    @GetMapping("/list")
    public R<Page<AiPrompt>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String scene,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) String keyword) {
        Page<AiPrompt> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<AiPrompt> qw = new LambdaQueryWrapper<>();
        qw.eq(AiPrompt::getDelFlag, 0);
        if (StringUtils.hasText(scene)) {
            qw.eq(AiPrompt::getScene, scene);
        }
        if (StringUtils.hasText(model)) {
            qw.eq(AiPrompt::getModel, model);
        }
        if (StringUtils.hasText(keyword)) {
            qw.and(w -> w.like(AiPrompt::getScene, keyword)
                    .or()
                    .like(AiPrompt::getDescription, keyword));
        }
        qw.orderByAsc(AiPrompt::getScene)
                .orderByAsc(AiPrompt::getModel)
                .orderByAsc(AiPrompt::getSortNo)
                .orderByDesc(AiPrompt::getCreateTime);
        return R.ok(aiPromptService.page(pageParam, qw));
    }

    /**
     * 详情
     */
    @GetMapping("/{id}")
    public R<AiPrompt> getById(@PathVariable Long id) {
        return R.ok(aiPromptService.getById(id));
    }

    /**
     * 新增
     */
    @PostMapping
    public R<Long> save(@RequestBody AiPrompt prompt) {
        normalize(prompt);
        if (existSceneModelVersion(prompt.getScene(), prompt.getModel(), prompt.getVersion(), null)) {
            return R.fail("该场景、模型、版本组合已存在");
        }
        aiPromptService.save(prompt);
        promptTemplateLoader.refresh(prompt.getScene());
        return R.ok(prompt.getId());
    }

    /**
     * 修改
     */
    @PutMapping
    public R<Long> update(@RequestBody AiPrompt prompt) {
        if (prompt.getId() == null) {
            return R.fail("ID 不能为空");
        }
        normalize(prompt);
        if (existSceneModelVersion(prompt.getScene(), prompt.getModel(), prompt.getVersion(), prompt.getId())) {
            return R.fail("该场景、模型、版本组合已存在");
        }
        aiPromptService.updateById(prompt);
        promptTemplateLoader.refresh(prompt.getScene());
        return R.ok(prompt.getId());
    }

    /**
     * 删除（逻辑删除）
     */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        AiPrompt prompt = aiPromptService.getById(id);
        aiPromptService.removeById(id);
        if (prompt != null) {
            promptTemplateLoader.refresh(prompt.getScene());
        }
        return R.ok();
    }

    /**
     * 刷新缓存
     */
    @PostMapping("/refresh/{scene}")
    public R<Void> refresh(@PathVariable String scene) {
        promptTemplateLoader.refresh(scene);
        return R.ok();
    }

    /**
     * 刷新全部缓存
     */
    @PostMapping("/refresh/all")
    public R<Void> refreshAll() {
        promptTemplateLoader.refreshAll();
        return R.ok();
    }

    /**
     * 场景列表（下拉选项）
     */
    @GetMapping("/scenes")
    public R<List<String>> scenes() {
        List<AiPrompt> list = aiPromptService.list(new LambdaQueryWrapper<AiPrompt>()
                .eq(AiPrompt::getDelFlag, 0)
                .select(AiPrompt::getScene));
        List<String> scenes = list.stream()
                .map(AiPrompt::getScene)
                .distinct()
                .sorted()
                .toList();
        return R.ok(scenes);
    }

    /**
     * 某场景下所有版本号（下拉选项）
     */
    @GetMapping("/versions")
    public R<List<String>> versions(@RequestParam String scene) {
        List<AiPrompt> list = aiPromptService.list(new LambdaQueryWrapper<AiPrompt>()
                .eq(AiPrompt::getDelFlag, 0)
                .eq(AiPrompt::getScene, scene)
                .select(AiPrompt::getVersion));
        List<String> versions = list.stream()
                .map(AiPrompt::getVersion)
                .distinct()
                .sorted()
                .toList();
        return R.ok(versions);
    }

    private boolean existSceneModelVersion(String scene, String model, String version, Long excludeId) {
        QueryWrapper<AiPrompt> qw = new QueryWrapper<>();
        qw.eq("scene", scene)
                .eq("model", model)
                .eq("version", version)
                .eq("del_flag", 0);
        if (excludeId != null) {
            qw.ne("id", excludeId);
        }
        return aiPromptService.count(qw) > 0;
    }

    private void normalize(AiPrompt prompt) {
        if (StringUtils.hasText(prompt.getScene())) {
            prompt.setScene(prompt.getScene().trim().toLowerCase());
        }
        if (StringUtils.hasText(prompt.getModel())) {
            prompt.setModel(prompt.getModel().trim().toLowerCase());
        } else {
            prompt.setModel("default");
        }
        if (StringUtils.hasText(prompt.getVersion())) {
            prompt.setVersion(prompt.getVersion().trim().toLowerCase());
        } else {
            prompt.setVersion("v1");
        }
        if (prompt.getEnabled() == null) {
            prompt.setEnabled(1);
        }
        if (prompt.getSortNo() == null) {
            prompt.setSortNo(0);
        }
    }
}
