package com.riverflow.ai.provider;

import com.riverflow.ai.config.AiProperties;
import com.riverflow.ai.model.entity.AiModel;
import com.riverflow.ai.model.service.AiModelService;
import com.riverflow.ai.provider.ollama.OllamaNativeProvider;
import com.riverflow.ai.provider.openai.OpenAiProvider;
import com.riverflow.ai.provider.qwen.QwenProvider;
import com.riverflow.ai.provider.zhipu.ZhipuProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * AI Provider 加载器
 *
 * <p>负责从配置文件或数据库加载 LLM Provider，并注册到 {@link AiProviderFactory}。
 */
@Slf4j
@Component
public class AiProviderLoader {

    private final AiProviderFactory providerFactory;

    public AiProviderLoader(AiProviderFactory providerFactory) {
        this.providerFactory = providerFactory;
    }

    /**
     * 从 application.yml 的配置加载 provider
     */
    public void loadFromConfig(AiProperties aiProperties) {
        if (aiProperties == null || aiProperties.getProviders() == null) {
            return;
        }
        for (AiProperties.Provider p : aiProperties.getProviders()) {
            AiProvider instance = createProvider(p.getType(), toConfig(p), aiProperties.getTimeout());
            if (instance != null) {
                providerFactory.register(p.getName(), instance);
                log.info("从配置注册 LLM provider: name={}, type={}, baseUrl={}",
                        p.getName(), p.getType(), p.getBaseUrl());
            }
        }
    }

    /**
     * 从数据库加载 provider（会清空已有 provider）
     */
    public void loadFromDatabase(AiModelService aiModelService, AiProperties aiProperties) {
        providerFactory.clear();
        List<AiModel> models;
        try {
            models = aiModelService.list();
        } catch (Exception e) {
            log.warn("从数据库加载 AI 模型配置失败，将回退到 application.yml 配置: {}", e.getMessage());
            loadFromConfig(aiProperties);
            return;
        }
        // 优先加载数据库中的模型配置
        if (models != null && !models.isEmpty()) {
            for (AiModel m : models) {
                if (m.getDelFlag() != null && m.getDelFlag() == 1) {
                    continue;
                }
                if (m.getStatus() != null && m.getStatus() == 0) {
                    continue;
                }
                AiProperties.Provider config = toConfig(m);
                AiProvider instance = createProvider(m.getProviderType(), config,
                        m.getTimeout() != null ? m.getTimeout() : aiProperties.getTimeout());
                if (instance != null) {
                    providerFactory.register(m.getModelCode(), instance);
                    log.info("从数据库注册 LLM provider: name={}, type={}, baseUrl={}",
                            m.getModelCode(), m.getProviderType(), m.getBaseUrl());
                }
            }
        }
        // 如果数据库没有可用配置，则回退到 YAML 配置作为兜底
        if (providerFactory.isEmpty()) {
            log.warn("数据库中未找到可用 AI 模型配置，回退到 application.yml 配置");
            loadFromConfig(aiProperties);
        }
        // 同步默认 provider 到 AiProperties，方便后续获取默认值
        updateDefaultProvider(aiProperties, models);
    }

    private AiProperties.Provider toConfig(AiProperties.Provider src) {
        return src;
    }

    private AiProperties.Provider toConfig(AiModel model) {
        AiProperties.Provider p = new AiProperties.Provider();
        p.setName(model.getModelCode());
        p.setType(model.getProviderType());
        p.setBaseUrl(model.getBaseUrl());
        p.setApiKey(model.getApiKey());
        p.setDefaultModel(model.getModelName());
        p.setTemperature(model.getTemperature() != null ? model.getTemperature() : 0.2f);
        p.setMaxTokens(model.getMaxTokens() != null ? model.getMaxTokens() : 4096);
        return p;
    }

    private AiProvider createProvider(String type, AiProperties.Provider provider, int timeout) {
        if (!StringUtils.hasText(type)) {
            return null;
        }
        return switch (type.toLowerCase()) {
            case OpenAiProvider.TYPE -> new OpenAiProvider(provider, timeout);
            case OllamaNativeProvider.TYPE -> new OllamaNativeProvider(provider, timeout);
            case ZhipuProvider.TYPE -> new ZhipuProvider(provider, timeout);
            case QwenProvider.TYPE -> new QwenProvider(provider, timeout);
            default -> {
                log.warn("未知的 LLM provider 类型: {}", type);
                yield null;
            }
        };
    }

    private void updateDefaultProvider(AiProperties aiProperties, List<AiModel> models) {
        String currentDefault = aiProperties.getDefaultProvider();
        if (StringUtils.hasText(currentDefault) && providerFactory.contains(currentDefault)) {
            return;
        }
        // 优先找 isDefault=1 的启用模型
        String defaultCode = models.stream()
                .filter(m -> m.getStatus() != null && m.getStatus() == 1)
                .filter(m -> m.getIsDefault() != null && m.getIsDefault() == 1)
                .map(AiModel::getModelCode)
                .filter(StringUtils::hasText)
                .findFirst()
                .orElse(null);
        // 没有默认模型则取第一个启用的模型
        if (!StringUtils.hasText(defaultCode)) {
            defaultCode = models.stream()
                    .filter(m -> m.getStatus() != null && m.getStatus() == 1)
                    .map(AiModel::getModelCode)
                    .filter(StringUtils::hasText)
                    .findFirst()
                    .orElse(null);
        }
        if (StringUtils.hasText(defaultCode)) {
            aiProperties.setDefaultProvider(defaultCode);
            log.info("重置默认 provider 为: {}", defaultCode);
        }
    }
}
