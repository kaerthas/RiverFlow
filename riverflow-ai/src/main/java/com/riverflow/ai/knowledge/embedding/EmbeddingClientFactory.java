package com.riverflow.ai.knowledge.embedding;

import com.riverflow.ai.config.AiProperties;
import com.riverflow.ai.knowledge.entity.AiVectorCollection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * EmbeddingClient 工厂
 *
 * <p>根据配置创建对应的 EmbeddingClient。支持 openai / ollama / qwen / zhipu。</p>
 */
@Slf4j
@Component
public class EmbeddingClientFactory {

    private final AiProperties aiProperties;

    @Autowired
    public EmbeddingClientFactory(AiProperties aiProperties) {
        this.aiProperties = aiProperties;
    }

    /**
     * 根据向量集合配置创建 EmbeddingClient
     *
     * <p>优先使用集合自身的 Embedding 配置，未配置则回退到全局配置。</p>
     */
    public EmbeddingClient create(AiVectorCollection collection) {
        if (collection == null) {
            return create();
        }
        AiProperties.EmbeddingConfig config = new AiProperties.EmbeddingConfig();
        config.setType(collection.getEmbeddingType());
        config.setBaseUrl(StringUtils.hasText(collection.getEmbeddingBaseUrl())
                ? collection.getEmbeddingBaseUrl()
                : aiProperties.getKnowledge().getEmbedding().getBaseUrl());
        config.setApiKey(StringUtils.hasText(collection.getEmbeddingApiKey())
                ? collection.getEmbeddingApiKey()
                : aiProperties.getKnowledge().getEmbedding().getApiKey());
        config.setModel(StringUtils.hasText(collection.getEmbeddingModel())
                ? collection.getEmbeddingModel()
                : aiProperties.getKnowledge().getEmbedding().getModel());
        config.setDimension(resolveDimension(collection.getDimension()));
        config.setTimeout(aiProperties.getKnowledge().getEmbedding().getTimeout());
        return create(config);
    }

    private int resolveDimension(Integer collectionDimension) {
        if (collectionDimension != null && collectionDimension > 0) {
            return collectionDimension;
        }
        return aiProperties.getKnowledge().getEmbedding().getDimension();
    }

    /**
     * 根据配置创建 EmbeddingClient
     */
    public EmbeddingClient create() {
        return create(aiProperties.getKnowledge().getEmbedding());
    }

    /**
     * 根据传入配置创建 EmbeddingClient（用于测试或临时场景）
     */
    public EmbeddingClient create(AiProperties.EmbeddingConfig config) {
        String type = config.getType();
        if (!StringUtils.hasText(type)) {
            type = "memory";
        }
        String typeLower = type.trim().toLowerCase();

        // 如果外部 Embedding 服务未配置 baseUrl，自动降级为内存测试模式
        if ((OpenAiEmbeddingClient.TYPE.equals(typeLower)
                || "qwen".equals(typeLower)
                || "zhipu".equals(typeLower)
                || "openai-compatible".equals(typeLower)
                || OllamaEmbeddingClient.TYPE.equals(typeLower))
                && !StringUtils.hasText(config.getBaseUrl())) {
            log.warn("Embedding 类型配置为 {} 但 baseUrl 为空，自动降级为 memory 测试模式", type);
            return new InMemoryEmbeddingClient(config.getDimension());
        }

        switch (typeLower) {
            case OpenAiEmbeddingClient.TYPE:
            case "qwen":
            case "zhipu":
            case "openai-compatible":
                return new OpenAiEmbeddingClient(
                        config.getBaseUrl(),
                        config.getApiKey(),
                        config.getModel(),
                        config.getDimension(),
                        config.getTimeout()
                );
            case OllamaEmbeddingClient.TYPE:
                return new OllamaEmbeddingClient(
                        config.getBaseUrl(),
                        config.getModel(),
                        config.getDimension(),
                        config.getTimeout()
                );
            case InMemoryEmbeddingClient.TYPE:
                return new InMemoryEmbeddingClient(config.getDimension());
            default:
                throw new IllegalArgumentException("不支持的 Embedding 类型: " + type);
        }
    }
}
