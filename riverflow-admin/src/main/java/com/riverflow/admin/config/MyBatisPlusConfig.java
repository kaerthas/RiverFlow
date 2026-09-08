package com.riverflow.admin.config;

import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * MyBatis Plus 配置
 */
@Slf4j
@Configuration
public class MyBatisPlusConfig {

    /**
     * 插件配置：分页 + 乐观锁
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 分页插件：不指定 DbType，按当前数据源 JDBC URL 自动识别（支持 MySQL / 达梦 DM8）
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        // 乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return interceptor;
    }

    /**
     * Map 结果集使用 key 大小写不敏感的 Map 实例。
     * 达梦/Oracle 把未加引号的标识符统一返回大写列名（如 APP_ID），MySQL 返回小写，
     * listMaps 等场景用 map.get("app_id") 在达梦下会取到 null。
     * 通过 ConfigurationCustomizer（MyBatis-Plus 明确装配的扩展点）让 MyBatis
     * 实例化结果 Map 时就创建 CASE_INSENSITIVE 的 TreeMap，写入与读取都忽略 key 大小写。
     */
    @Bean
    public ConfigurationCustomizer caseInsensitiveMapCustomizer() {
        return configuration -> configuration.setObjectFactory(new DefaultObjectFactory() {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T create(Class<T> type, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
                if (type != null && Map.class.isAssignableFrom(type)
                        && (constructorArgs == null || constructorArgs.isEmpty())) {
                    return (T) new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
                }
                return super.create(type, constructorArgTypes, constructorArgs);
            }
        });
    }

    /**
     * 自动填充处理器
     */
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject metaObject) {
                this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
                this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
                this.strictInsertFill(metaObject, "delFlag", Integer.class, 0);
            }

            @Override
            public void updateFill(MetaObject metaObject) {
                this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
            }
        };
    }
}
