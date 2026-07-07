package com.riverflow.ai.provider;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * LLM Provider 工厂
 *
 * <p>管理所有注册的 {@link AiProvider} 实例，按 provider 名称路由。
 */
@Component
public class AiProviderFactory {

    private final Map<String, AiProvider> providers = new ConcurrentHashMap<>();

    /**
     * 注册 provider
     */
    public void register(String name, AiProvider provider) {
        providers.put(name, provider);
    }

    /**
     * 按名称获取 provider
     */
    public AiProvider getProvider(String name) {
        AiProvider provider = providers.get(name);
        if (provider == null) {
            throw new IllegalArgumentException("未找到名称为 " + name + " 的 LLM provider");
        }
        return provider;
    }

    /**
     * 获取默认 provider
     */
    public AiProvider getDefaultProvider(String defaultName) {
        return getProvider(defaultName);
    }

    /**
     * 清空所有 provider
     */
    public void clear() {
        providers.clear();
    }

    /**
     * 是否为空
     */
    public boolean isEmpty() {
        return providers.isEmpty();
    }

    /**
     * 是否包含指定 provider
     */
    public boolean contains(String name) {
        return providers.containsKey(name);
    }

    /**
     * 获取第一个 provider 名称
     */
    public String firstName() {
        return providers.keySet().stream().findFirst().orElse(null);
    }

    /**
     * 获取所有 provider 名称
     */
    public Collection<String> getProviderNames() {
        return providers.keySet();
    }
}
