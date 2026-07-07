package com.riverflow.ai.cache;

import com.alibaba.fastjson2.JSON;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.riverflow.ai.provider.AiChatRequest;
import com.riverflow.ai.provider.AiChatResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * AI 调用结果缓存管理器
 *
 * <p>对相同 Prompt 的调用结果进行缓存，降低 LLM 调用成本和延迟。
 * 缓存 Key 由 provider + model + messages 内容计算 MD5 得到。
 */
@Slf4j
@Component
public class AiCacheManager {

    private final Cache<String, AiChatResponse> cache;

    public AiCacheManager() {
        this.cache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .recordStats()
                .build();
    }

    /**
     * 生成缓存 Key
     */
    public String buildKey(String provider, AiChatRequest request) {
        String model = request.getModel() != null ? request.getModel() : "default";
        String content = provider + ":" + model + ":" + JSON.toJSONString(request.getMessages());
        return cn.hutool.crypto.SecureUtil.md5(content);
    }

    /**
     * 获取缓存
     */
    public AiChatResponse get(String key) {
        AiChatResponse response = cache.getIfPresent(key);
        if (response != null) {
            log.debug("AI 缓存命中: key={}", key);
        }
        return response;
    }

    /**
     * 写入缓存
     */
    public void put(String key, AiChatResponse response) {
        if (response == null || response.getContent() == null) {
            return;
        }
        cache.put(key, response);
        log.debug("AI 缓存写入: key={}", key);
    }

    /**
     * 清空缓存
     */
    public void clear() {
        cache.invalidateAll();
        log.info("AI 缓存已清空");
    }
}
