package com.riverflow.ai.provider;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * LLM 对话消息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiMessage {

    /**
     * 角色：system / user / assistant
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;

    public static AiMessage system(String content) {
        return new AiMessage("system", content);
    }

    public static AiMessage user(String content) {
        return new AiMessage("user", content);
    }

    public static AiMessage assistant(String content) {
        return new AiMessage("assistant", content);
    }
}
