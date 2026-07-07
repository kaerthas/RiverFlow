package com.riverflow.ai.provider.qwen;

import com.riverflow.ai.config.AiProperties;
import com.riverflow.ai.provider.AiChatRequest;
import com.riverflow.ai.provider.AiChatResponse;
import com.riverflow.ai.provider.AiProvider;
import com.riverflow.ai.provider.openai.OpenAiProvider;

import java.util.function.Consumer;

/**
 * 通义千问 Provider
 *
 * <p>通义千问提供 OpenAI 兼容接口，内部委托给 {@link OpenAiProvider} 实现。
 * 独立 Provider 便于未来接入通义千问特有的能力（如工具调用、联网搜索、长文本等）。
 */
public class QwenProvider implements AiProvider {

    public static final String TYPE = "qwen";

    private final OpenAiProvider delegate;

    public QwenProvider(AiProperties.Provider providerConfig, int timeout) {
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
