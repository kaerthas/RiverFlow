package com.riverflow.ai.config;

import com.riverflow.ai.model.service.AiModelService;
import com.riverflow.ai.provider.AiProviderFactory;
import com.riverflow.ai.provider.AiProviderLoader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI 能力自动配置
 *
 * <p>根据配置文件中的 provider 列表动态创建 {@link com.riverflow.ai.provider.AiProvider} Bean，
 * 并注册到 {@link AiProviderFactory} 中。
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "riverflow.ai", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AiAutoConfiguration {

    @Bean
    public AiProviderFactory aiProviderFactory() {
        return new AiProviderFactory();
    }

    @Bean
    public AiProviderLoader aiProviderLoader(AiProviderFactory aiProviderFactory) {
        return new AiProviderLoader(aiProviderFactory);
    }

    /**
     * 应用启动后，优先从数据库加载 AI 模型配置；数据库无可用配置时回退到 application.yml。
     */
    @Bean
    public ApplicationRunner aiProviderInitializer(AiProviderLoader loader,
                                                   AiModelService aiModelService,
                                                   AiProperties aiProperties) {
        return args -> loader.loadFromDatabase(aiModelService, aiProperties);
    }
}
