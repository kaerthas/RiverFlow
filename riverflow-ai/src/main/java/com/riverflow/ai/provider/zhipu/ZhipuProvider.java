package com.riverflow.ai.provider.zhipu;

import com.riverflow.ai.config.AiProperties;
import com.riverflow.ai.provider.AiChatRequest;
import com.riverflow.ai.provider.AiChatResponse;
import com.riverflow.ai.provider.AiProvider;
import com.riverflow.ai.provider.openai.OpenAiProvider;

import java.util.function.Consumer;

/**
 * 智谱 AI Provider
 *
 * <p>智谱 GLM 提供 OpenAI 兼容接口，因此内部委托给 {@link OpenAiProvider} 实现。
 * 独立 Provider 的目的是便于未来接入智谱特有的能力（如工具调用、联网搜索）。
 */
public class ZhipuProvider implements AiProvider {

    public static final String TYPE = "zhipu";

    private final OpenAiProvider delegate;

    public ZhipuProvider(AiProperties.Provider providerConfig, int timeout) {
        this.delegate = new OpenAiProvider(providerConfig, timeout);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public boolean supports(String model) {
        return delegate.supports(model);
    }

    @Override
    public AiChatResponse chat(AiChatRequest request) {
        return delegate.chat(request);
    }

    @Override
    public void stream(AiChatRequest request, Consumer<String> onData, Consumer<Throwable> onError, Runnable onComplete) {
        delegate.stream(request, onData, onError, onComplete);
    }
}
