package com.inspur.workinfo.config;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisPlusConfig {
    /**
     * 分页处理
     * @return
     */
    @Bean
//    @ConditionalOnMissingBean
    public PaginationInterceptor paginationInterceptor(){
        return new PaginationInterceptor();
    }
}
