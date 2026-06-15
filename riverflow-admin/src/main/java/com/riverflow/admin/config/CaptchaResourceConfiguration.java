package com.riverflow.admin.config;

import cloud.tianai.captcha.common.constant.CaptchaTypeConstant;
import cloud.tianai.captcha.resource.FontCache;
import cloud.tianai.captcha.resource.ResourceStore;
import cloud.tianai.captcha.resource.common.model.dto.Resource;
import cloud.tianai.captcha.resource.impl.LocalMemoryResourceStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Arrays;
import java.util.List;

/**
 * tianai-captcha 资源加载配置
 * <p>
 * 显式加载系统默认背景图与字体资源，避免默认自动装配时资源数量不足。
 * 使用 {@link Primary} 确保覆盖 starter 自动配置的 RedisResourceStore。
 */
@Slf4j
@Configuration
public class CaptchaResourceConfiguration {

    /**
     * 滑块/旋转/拼接/文字点选验证码共用的背景图资源。
     * 增加图片数量后，TAC 会在生成验证码时随机选取背景图，并随机计算滑块缺口位置。
     */
    private static final List<String> BACKGROUND_IMAGES = Arrays.asList(
            "META-INF/cut-image/resource/1.jpg",
            "META-INF/cut-image/resource/bg1.jpg",
            "META-INF/cut-image/resource/bg2.jpg",
            "META-INF/cut-image/resource/bg3.jpg",
            "META-INF/cut-image/resource/bg4.jpg",
            "META-INF/cut-image/resource/bg5.jpg"
    );

    @Primary
    @Bean
    public ResourceStore resourceStore() {
        LocalMemoryResourceStore resourceStore = new LocalMemoryResourceStore();

        // 加载 tianai-captcha 内置背景图与自定义背景图
        for (String imagePath : BACKGROUND_IMAGES) {
            addResource(resourceStore, CaptchaTypeConstant.SLIDER, imagePath);
            addResource(resourceStore, CaptchaTypeConstant.ROTATE, imagePath);
            addResource(resourceStore, CaptchaTypeConstant.CONCAT, imagePath);
            addResource(resourceStore, CaptchaTypeConstant.WORD_IMAGE_CLICK, imagePath);
        }

        // 加载 tianai-captcha 内置字体（供文字点选验证码使用）
        addFont(resourceStore, "classpath", "META-INF/cut-image/template/fonts/SIMSUN.TTC", "default");

        log.info("tianai-captcha 资源加载完成，共加载 {} 张背景图", BACKGROUND_IMAGES.size());
        return resourceStore;
    }

    private void addResource(LocalMemoryResourceStore store, String type, String classpath) {
        try {
            store.addResource(type, new Resource("classpath", classpath, "default"));
        } catch (Exception e) {
            log.warn("加载验证码背景图资源失败: type={}, path={}", type, classpath, e);
        }
    }

    private void addFont(LocalMemoryResourceStore store, String resourceType, String path, String tag) {
        try {
            store.addResource(FontCache.FONT_TYPE, new Resource(resourceType, path, tag));
        } catch (Exception e) {
            log.warn("加载验证码字体资源失败: path={}", path, e);
        }
    }
}
