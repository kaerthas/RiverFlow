package com.riverflow.common.spring;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Spring 上下文持有者
 * 用于在非 Spring 管理的类（如插件）中获取 Spring Bean
 *
 * 使用场景：
 * 1. 流程节点插件通过 {@link com.riverflow.api.plugin.NodePlugin#init(ApplicationContext)} 获取上下文
 * 2. 静态工具类中获取 Bean
 */
public class SpringContextHolder implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext context) {
        SpringContextHolder.applicationContext = context;
    }

    /**
     * 获取 ApplicationContext
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * 通过类型获取 Bean
     */
    public static <T> T getBean(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }

    /**
     * 通过名称和类型获取 Bean
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        return applicationContext.getBean(name, clazz);
    }

    /**
     * 检查是否已初始化
     */
    public static boolean isInitialized() {
        return applicationContext != null;
    }
}
