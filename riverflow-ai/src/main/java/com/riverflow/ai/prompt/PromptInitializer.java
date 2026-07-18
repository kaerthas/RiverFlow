package com.riverflow.ai.prompt;

import com.riverflow.ai.prompt.entity.AiPrompt;
import com.riverflow.ai.prompt.service.AiPromptService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * Prompt 默认数据初始化器
 *
 * <p>应用启动时，如果数据库中没有任何 Prompt，自动将 classpath 中的默认 Prompt 文件导入数据库。</p>
 * <p>这样新部署环境无需手动执行 SQL，也能在「AI Prompt 管理」页面看到默认 Prompt。</p>
 */
@Slf4j
@Component
public class PromptInitializer {

    private static final String PROMPT_DIR = "ai-prompts/";
    private static final String DEFAULT_MODEL = "default";
    private static final String DEFAULT_VERSION = "v1";

    private final AiPromptService aiPromptService;

    @Autowired
    public PromptInitializer(AiPromptService aiPromptService) {
        this.aiPromptService = aiPromptService;
    }

    @PostConstruct
    public void init() {
        try {
            long count = aiPromptService.count();
            if (count > 0) {
                log.info("[PromptInitializer] 数据库中已有 {} 条 Prompt，跳过默认数据初始化", count);
                return;
            }
            log.info("[PromptInitializer] 数据库中暂无 Prompt，开始从 classpath 导入默认数据");
            importDefaultPrompts();
        } catch (Exception e) {
            log.warn("[PromptInitializer] 初始化默认 Prompt 数据失败，将使用 classpath 兜底: {}", e.getMessage());
        }
    }

    private void importDefaultPrompts() throws Exception {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:" + PROMPT_DIR + "*.prompt");

        List<String> scenes = Arrays.stream(resources)
                .map(r -> r.getFilename())
                .filter(name -> name != null && name.endsWith(".prompt"))
                .map(name -> name.substring(0, name.length() - ".prompt".length()))
                .filter(name -> !name.endsWith(".system"))
                .sorted()
                .toList();

        int imported = 0;
        for (String scene : scenes) {
            String template = readClasspathFile(PROMPT_DIR + scene + ".prompt");
            String systemPrompt = readClasspathFileIfExists(PROMPT_DIR + scene + ".system.prompt");
            String outputSchema = readClasspathFileIfExists(PROMPT_DIR + scene + ".schema.json");

            if (!StringUtils.hasText(template)) {
                continue;
            }

            AiPrompt prompt = new AiPrompt();
            prompt.setScene(scene);
            prompt.setModel(DEFAULT_MODEL);
            prompt.setVersion(DEFAULT_VERSION);
            prompt.setTemplate(template);
            prompt.setSystemPrompt(systemPrompt);
            prompt.setOutputSchema(outputSchema);
            prompt.setExamples("[]");
            prompt.setDescription(scene + " 默认 Prompt");
            prompt.setEnabled(1);
            prompt.setSortNo(0);
            aiPromptService.save(prompt);
            imported++;
            log.info("[PromptInitializer] 已导入默认 Prompt: scene={}, model={}, version={}",
                    scene, DEFAULT_MODEL, DEFAULT_VERSION);
        }

        log.info("[PromptInitializer] 默认 Prompt 初始化完成，共导入 {} 条", imported);
    }

    private String readClasspathFile(String path) throws Exception {
        ClassPathResource resource = new ClassPathResource(path);
        try (InputStream is = resource.getInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private String readClasspathFileIfExists(String path) {
        ClassPathResource resource = new ClassPathResource(path);
        if (!resource.exists()) {
            return null;
        }
        try (InputStream is = resource.getInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.warn("[PromptInitializer] 读取 classpath 资源失败: {}", path, e);
            return null;
        }
    }
}
