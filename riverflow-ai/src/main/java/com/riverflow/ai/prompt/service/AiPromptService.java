package com.riverflow.ai.prompt.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.riverflow.ai.prompt.entity.AiPrompt;

import java.util.List;

/**
 * AI Prompt 模板 Service
 */
public interface AiPromptService extends IService<AiPrompt> {

    /**
     * 获取启用的 Prompt，优先匹配具体 model，否则取 default
     *
     * @param scene   场景
     * @param model   模型，如 qwen2.5:14b
     * @param version 版本，如 v1
     * @return Prompt 模板，找不到返回 null
     */
    AiPrompt getEnabledPrompt(String scene, String model, String version);

    /**
     * 查询某场景下所有启用的 Prompt
     */
    List<AiPrompt> listEnabledByScene(String scene);

    /**
     * 获取默认版本号（某场景 + 某模型下启用的第一个版本）
     */
    String getDefaultVersion(String scene, String model);
}
