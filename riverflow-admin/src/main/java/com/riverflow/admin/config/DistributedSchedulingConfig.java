package com.riverflow.admin.config;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

/**
 * 分布式调度配置
 * 使用 ShedLock 确保多节点部署时同一时刻只有一个节点执行定时调度任务。
 * 注意：@EnableScheduling 已在 AdminApplication 上开启，此处不再重复。
 */
@Configuration
@EnableSchedulerLock(defaultLockAtMostFor = "PT9S")
public class DistributedSchedulingConfig {

    /**
     * Redis 锁提供者（锁 key 默认前缀 job-lock:）
     */
    @Bean
    public LockProvider lockProvider(RedisConnectionFactory connectionFactory) {
        return new RedisLockProvider(connectionFactory);
    }
}
