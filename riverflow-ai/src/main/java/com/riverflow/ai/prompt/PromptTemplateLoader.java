package com.riverflow.ai.prompt;

import cn.hutool.core.io.IoUtil;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Prompt 模板加载器
 *
 * <p>从 classpath:ai-prompts/ 目录加载 .prompt 文件并缓存。
 */
@Component
public class PromptTemplateLoader {

    private static final String PROMPT_DIR = "ai-prompts/";
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    /**
     * 加载模板
     */
    public String load(String name) {
        return cache.computeIfAbsent(name, k -> {
            String path = PROMPT_DIR + k + ".prompt";
            try (InputStream is = new ClassPathResource(path).getInputStream()) {
                return IoUtil.read(is, StandardCharsets.UTF_8);
            } catch (Exception e) {
                throw new IllegalStateException("无法加载 Prompt 模板: " + path, e);
            }
        });
    }
}
