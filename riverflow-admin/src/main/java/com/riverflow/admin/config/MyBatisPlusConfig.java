package com.riverflow.admin.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;
import java.time.LocalDateTime;

/**
 * MyBatis Plus 配置
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(MybatisPlusProperties.class)
public class MyBatisPlusConfig {

    /**
     * 插件配置：分页 + 乐观锁
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        // 乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return interceptor;
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

    /**
     * 手动创建 SqlSessionFactory，避免 Spring Boot 3 自动配置顺序问题
     */
    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource,
                                                MybatisPlusProperties properties,
                                                MybatisPlusInterceptor mybatisPlusInterceptor) throws Exception {
        MybatisSqlSessionFactoryBean factoryBean = new MybatisSqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);

        // mapper 扫描路径（仅在存在对应资源时设置，避免启动失败）
        if (properties.getMapperLocations() != null && properties.getMapperLocations().length > 0) {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            try {
                org.springframework.core.io.Resource[] resources = resolver.getResources(properties.getMapperLocations()[0]);
                if (resources.length > 0) {
                    factoryBean.setMapperLocations(resources);
                }
            } catch (Exception e) {
                log.warn("MyBatis mapper XML 路径解析失败，跳过: {}", properties.getMapperLocations()[0]);
            }
        }

        // 类型别名包
        if (properties.getTypeAliasesPackage() != null && !properties.getTypeAliasesPackage().isEmpty()) {
            factoryBean.setTypeAliasesPackage(properties.getTypeAliasesPackage());
        }

        // 插件
        factoryBean.setPlugins(mybatisPlusInterceptor);

        // 全局配置（驼峰映射、缓存等）
        if (properties.getConfiguration() != null) {
            MybatisConfiguration configuration = new MybatisConfiguration();
            properties.getConfiguration().applyTo(configuration);
            factoryBean.setConfiguration(configuration);
        }
        if (properties.getGlobalConfig() != null) {
            factoryBean.setGlobalConfig(properties.getGlobalConfig());
        }

        return factoryBean.getObject();
    }

    /**
     * 手动创建 SqlSessionTemplate
     */
    @Bean
    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}
