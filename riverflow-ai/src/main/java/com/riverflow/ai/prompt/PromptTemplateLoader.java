package com.riverflow.ai.prompt;

import cn.hutool.core.io.IoUtil;
import com.riverflow.ai.prompt.dto.PromptContent;
import com.riverflow.ai.prompt.entity.AiPrompt;
import com.riverflow.ai.prompt.service.AiPromptService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Prompt 模板加载器
 *
 * <p>加载策略：数据库优先，classpath 文件兜底。支持热刷新。</p>
 */
@Slf4j
@Component
public class PromptTemplateLoader {

    private static final String PROMPT_DIR = "ai-prompts/";
    private static final String DEFAULT_MODEL = "default";
    private static final String DEFAULT_VERSION = "v1";

    private final AiPromptService aiPromptService;

    /**
     * 缓存：key = scene:model:version
     */
    private final Map<String, PromptContent> cache = new ConcurrentHashMap<>();

    @Autowired
    public PromptTemplateLoader(AiPromptService aiPromptService) {
        this.aiPromptService = aiPromptService;
    }

    @PostConstruct
    public void init() {
        log.info("[Prompt] 初始化 Prompt 模板加载器，策略：数据库优先，classpath 兜底");
    }

    /**
     * 加载 Prompt 内容（使用默认 model 和 version）
     */
    public PromptContent load(String scene) {
        return load(scene, DEFAULT_MODEL, DEFAULT_VERSION);
    }

    /**
     * 加载 Prompt 内容（指定 model 和 version，为空时使用默认值）
     */
    public PromptContent load(String scene, String model, String version) {
        String actualModel = StringUtils.hasText(model) ? model : DEFAULT_MODEL;
        String actualVersion = StringUtils.hasText(version) ? version : DEFAULT_VERSION;
        String cacheKey = buildCacheKey(scene, actualModel, actualVersion);
        PromptContent cached = cache.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        PromptContent content = loadFromDatabase(scene, actualModel, actualVersion)
                .orElseGet(() -> loadFromClasspath(scene, actualModel, actualVersion));

        if (content != null) {
            cache.put(cacheKey, content);
        }
        return content;
    }

    /**
     * 加载 Prompt 模板字符串（兼容旧接口）
     */
    public String loadTemplate(String scene) {
        PromptContent content = load(scene);
        return content != null ? content.getTemplate() : null;
    }

    /**
     * 刷新指定场景的缓存
     */
    public void refresh(String scene) {
        cache.entrySet().removeIf(e -> e.getKey().startsWith(scene + ":"));
        log.info("[Prompt] 已刷新场景 [{}] 的缓存", scene);
    }

    /**
     * 刷新所有缓存
     */
    public void refreshAll() {
        cache.clear();
        log.info("[Prompt] 已刷新所有 Prompt 缓存");
    }

    /**
     * 从数据库加载
     */
    private java.util.Optional<PromptContent> loadFromDatabase(String scene, String model, String version) {
        if (aiPromptService == null) {
            return java.util.Optional.empty();
        }
        try {
            AiPrompt prompt = aiPromptService.getEnabledPrompt(scene, model, version);
            if (prompt == null || !StringUtils.hasText(prompt.getTemplate())) {
                return java.util.Optional.empty();
            }
            return java.util.Optional.of(PromptContent.builder()
                    .scene(scene)
                    .model(model)
                    .version(version)
                    .systemPrompt(prompt.getSystemPrompt())
                    .template(prompt.getTemplate())
                    .examples(prompt.getExamples())
                    .outputSchema(prompt.getOutputSchema())
                    .source("database")
                    .build());
        } catch (Exception e) {
            log.warn("[Prompt] 从数据库加载 Prompt 失败，scene={}, model={}, version={}", scene, model, version, e);
            return java.util.Optional.empty();
        }
    }

    /**
     * 从 classpath 文件加载
     */
    private PromptContent loadFromClasspath(String scene, String model, String version) {
        String templatePath = PROMPT_DIR + scene + ".prompt";
        String systemPromptPath = PROMPT_DIR + scene + ".system.prompt";
        String schemaPath = PROMPT_DIR + scene + ".schema.json";

        String template = readClasspathFile(templatePath);
        String systemPrompt = readClasspathFileIfExists(systemPromptPath);
        String outputSchema = readClasspathFileIfExists(schemaPath);

        log.info("[Prompt] 从 classpath 加载 Prompt：scene={}, path={}", scene, templatePath);
        return PromptContent.builder()
                .scene(scene)
                .model(model)
                .version(version)
                .systemPrompt(systemPrompt)
                .template(template)
                .outputSchema(outputSchema)
                .source("classpath")
                .build();
    }

    private String readClasspathFile(String path) {
        try (InputStream is = new ClassPathResource(path).getInputStream()) {
            return IoUtil.read(is, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("无法加载 Prompt 资源: " + path, e);
        }
    }

    private String readClasspathFileIfExists(String path) {
        ClassPathResource resource = new ClassPathResource(path);
        if (!resource.exists()) {
            return null;
        }
        try (InputStream is = resource.getInputStream()) {
            return IoUtil.read(is, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.warn("[Prompt] 读取 classpath 资源失败: {}", path, e);
            return null;
        }
    }

    private String buildCacheKey(String scene, String model, String version) {
        return scene + ":" + model + ":" + version;
    }
}
