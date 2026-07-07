package com.riverflow.ai.client;

import com.riverflow.ai.cache.AiCacheManager;
import com.riverflow.ai.config.AiProperties;
import com.riverflow.ai.limit.AiRateLimiter;
import com.riverflow.ai.provider.AiChatRequest;
import com.riverflow.ai.provider.AiChatResponse;
import com.riverflow.ai.provider.AiProvider;
import com.riverflow.ai.provider.AiProviderFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * AI 聊天统一客户端
 *
 * <p>业务层统一通过此类调用 LLM，屏蔽底层 provider 差异。
 * 支持指定 provider、失败重试、超时控制、结果缓存、限流保护。
 */
@Slf4j
@Component
public class AiChatClient {

    private final AiProperties aiProperties;
    private final AiProviderFactory providerFactory;
    private final AiCacheManager cacheManager;
    private final AiRateLimiter rateLimiter;

    @Autowired
    public AiChatClient(AiProperties aiProperties, AiProviderFactory providerFactory,
                        AiCacheManager cacheManager, AiRateLimiter rateLimiter) {
        this.aiProperties = aiProperties;
        this.providerFactory = providerFactory;
        this.cacheManager = cacheManager;
        this.rateLimiter = rateLimiter;
    }

    /**
     * 使用默认 provider 对话
     */
    public AiChatResponse chat(AiChatRequest request) {
        return chat(aiProperties.getDefaultProvider(), request);
    }

    /**
     * 使用默认 provider 对话（带用户标识）
     */
    public AiChatResponse chat(AiChatRequest request, String userId) {
        return chat(aiProperties.getDefaultProvider(), request, userId);
    }

    /**
     * 使用指定 provider 对话
     */
    public AiChatResponse chat(String providerName, AiChatRequest request) {
        return chat(providerName, request, "system");
    }

    /**
     * 使用指定 provider 对话（带用户标识用于限流）
     */
    public AiChatResponse chat(String providerName, AiChatRequest request, String userId) {
        if (!rateLimiter.tryAcquire(userId != null ? userId : "anonymous")) {
            throw new RuntimeException("AI 调用过于频繁，请稍后再试");
        }

        String cacheKey = cacheManager.buildKey(providerName, request);
        AiChatResponse cached = cacheManager.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        AiProvider provider = providerFactory.getProvider(providerName);
        int maxRetries = Math.max(0, aiProperties.getRetry());

        RuntimeException lastException = null;
        for (int i = 0; i <= maxRetries; i++) {
            try {
                AiChatResponse response = provider.chat(request);
                cacheManager.put(cacheKey, response);
                return response;
            } catch (Exception e) {
                lastException = e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e);
                log.warn("LLM 调用失败，准备第 {} 次重试: provider={}, scene={}", i + 1, providerName, request.getScene(), e);
            }
        }
        throw lastException != null ? lastException : new RuntimeException("LLM 调用失败且未捕获异常");
    }

    /**
     * 便捷方法：单条 system + user 消息对话
     */
    public AiChatResponse chat(String systemPrompt, String userPrompt, String responseFormat, String scene) {
        AiChatRequest request = AiChatRequest.builder()
                .messages(List.of(
                        com.riverflow.ai.provider.AiMessage.system(systemPrompt),
                        com.riverflow.ai.provider.AiMessage.user(userPrompt)
                ))
                .responseFormat(responseFormat)
                .scene(scene)
                .build();
        return chat(request);
    }

    /**
     * 流式对话（默认 provider）
     */
    public void stream(AiChatRequest request, java.util.function.Consumer<String> onData,
                       java.util.function.Consumer<Throwable> onError, Runnable onComplete) {
        stream(aiProperties.getDefaultProvider(), request, onData, onError, onComplete);
    }

    /**
     * 流式对话（指定 provider）
     */
    public void stream(String providerName, AiChatRequest request,
                       java.util.function.Consumer<String> onData,
                       java.util.function.Consumer<Throwable> onError, Runnable onComplete) {
        AiProvider provider = providerFactory.getProvider(providerName);
        provider.stream(request, onData, onError, onComplete);
    }
}
