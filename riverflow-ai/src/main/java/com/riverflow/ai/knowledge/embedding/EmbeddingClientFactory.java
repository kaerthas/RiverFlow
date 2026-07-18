package com.riverflow.ai.knowledge.embedding;

import com.riverflow.ai.config.AiProperties;
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
     * 根据配置创建 EmbeddingClient
     */
    public EmbeddingClient create() {
        AiProperties.EmbeddingConfig config = aiProperties.getKnowledge().getEmbedding();
        String type = config.getType();
        if (!StringUtils.hasText(type)) {
            type = "openai";
        }
        String typeLower = type.trim().toLowerCase();

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
            default:
                throw new IllegalArgumentException("不支持的 Embedding 类型: " + type);
        }
    }
}
