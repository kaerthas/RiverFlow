package com.riverflow.ai.provider;

import java.util.function.Consumer;

/**
 * LLM Provider SPI
 *
 * <p>不同模型厂商的实现类只需实现此接口并注册为 Spring Bean，
 * 即可被 {@link com.riverflow.ai.client.AiChatClient} 统一调用。
 */
public interface AiProvider {

    /**
     * provider 类型，如 openai / ollama / zhipu
     */
    String getType();

    /**
     * 是否支持指定模型
     */
    boolean supports(String model);

    /**
     * 执行对话
     */
    AiChatResponse chat(AiChatRequest request);

    /**
     * 流式对话
     *
     * @param request AI 请求
     * @param onData  每次收到数据回调
     * @param onError 异常回调
     * @param onComplete 完成回调
     */
    default void stream(AiChatRequest request, Consumer<String> onData, Consumer<Throwable> onError, Runnable onComplete) {
        throw new UnsupportedOperationException("当前 provider 不支持流式输出");
    }
}
