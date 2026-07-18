package com.riverflow.ai.prompt.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.riverflow.ai.prompt.entity.AiPrompt;
import com.riverflow.ai.prompt.mapper.AiPromptMapper;
import com.riverflow.ai.prompt.service.AiPromptService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI Prompt 模板 Service 实现
 */
@Service
public class AiPromptServiceImpl extends ServiceImpl<AiPromptMapper, AiPrompt>
        implements AiPromptService {

    @Override
    public AiPrompt getEnabledPrompt(String scene, String model, String version) {
        return baseMapper.findEnabledBySceneAndModelAndVersion(scene, model, version);
    }

    @Override
    public List<AiPrompt> listEnabledByScene(String scene) {
        return baseMapper.findEnabledByScene(scene);
    }

    @Override
    public String getDefaultVersion(String scene, String model) {
        AiPrompt prompt = baseMapper.findEnabledBySceneAndModelAndVersion(scene, model, "v1");
        if (prompt != null) {
            return prompt.getVersion();
        }
        List<AiPrompt> list = baseMapper.findEnabledByScene(scene);
        return list.isEmpty() ? "v1" : list.get(0).getVersion();
    }
}
