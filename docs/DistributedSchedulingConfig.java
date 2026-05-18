package com.riverflow.admin.config;

import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 分布式调度配置
 * 使用 ShedLock 确保同一时刻只有一个节点执行调度任务
 */
@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "9s")
public class DistributedSchedulingConfig {

    /**
     * Redis 锁提供者
     */
    @Bean
    public LockProvider lockProvider(RedisConnectionFactory connectionFactory) {
        return new net.javacrumbs.shedlock.provider.redis.RedisLockProvider(connectionFactory);
    }
}
