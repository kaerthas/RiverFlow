package com.riverflow.ai.knowledge.controller;

import com.riverflow.ai.config.AiProperties;
import com.riverflow.ai.knowledge.dto.EmbeddingTestRequest;
import com.riverflow.ai.knowledge.dto.EmbeddingTestResponse;
import com.riverflow.ai.knowledge.embedding.EmbeddingClient;
import com.riverflow.ai.knowledge.embedding.EmbeddingClientFactory;
import com.riverflow.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Embedding 配置测试接口
 */
@Slf4j
@RestController
@RequestMapping("/ai/embedding")
public class AiEmbeddingConfigController {

    private final EmbeddingClientFactory embeddingClientFactory;

    @Autowired
    public AiEmbeddingConfigController(EmbeddingClientFactory embeddingClientFactory) {
        this.embeddingClientFactory = embeddingClientFactory;
    }

    /**
     * 测试 Embedding 连接
     */
    @PostMapping("/test")
    public R<EmbeddingTestResponse> test(@RequestBody EmbeddingTestRequest request) {
        AiProperties.EmbeddingConfig config = new AiProperties.EmbeddingConfig();
        config.setType(StringUtils.hasText(request.getType()) ? request.getType() : "memory");
        config.setBaseUrl(request.getBaseUrl());
        config.setApiKey(request.getApiKey());
        config.setModel(StringUtils.hasText(request.getModel()) ? request.getModel() : getDefaultModel(config.getType()));
        config.setDimension(request.getDimension() != null && request.getDimension() > 0 ? request.getDimension() : 768);
        config.setTimeout(request.getTimeout() != null && request.getTimeout() > 0 ? request.getTimeout() : 30000);

        String testText = StringUtils.hasText(request.getText()) ? request.getText() : "RiverFlow 是一个流程编排平台";

        long start = System.currentTimeMillis();
        try {
            EmbeddingClient client = embeddingClientFactory.create(config);
            List<float[]> embeddings = client.embed(List.of(testText));
            long elapsed = System.currentTimeMillis() - start;
            if (embeddings.isEmpty() || embeddings.get(0) == null) {
                return R.ok(EmbeddingTestResponse.fail("Embedding 返回结果为空"));
            }
            float[] vector = embeddings.get(0);
            return R.ok(EmbeddingTestResponse.success(
                    "Embedding 连接测试成功",
                    vector.length,
                    elapsed,
                    config.getModel()
            ));
        } catch (Exception e) {
            log.error("Embedding 连接测试失败", e);
            return R.ok(EmbeddingTestResponse.fail("Embedding 连接测试失败: " + e.getMessage()));
        }
    }

    private String getDefaultModel(String type) {
        return switch (type.trim().toLowerCase()) {
            case "ollama" -> "nomic-embed-text";
            case "openai", "openai-compatible" -> "text-embedding-3-small";
            case "qwen" -> "text-embedding-v2";
            case "zhipu" -> "embedding-2";
            default -> "text-embedding-3-small";
        };
    }
}
