package com.riverflow.admin.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * Redisson 配置
 *
 * <p>采用编程式配置，解决 YAML 中密码为空时 Redisson 仍会发送 AUTH 命令的问题。</p>
 */
@Configuration
@ConditionalOnProperty(prefix = "riverflow.distributed", name = "redisson-lock-enabled", havingValue = "true", matchIfMissing = true)
public class RedissonConfig {

    @Value("${spring.data.redis.host:127.0.0.1}")
    private String host;

    @Value("${spring.data.redis.port:6379}")
    private int port;

    @Value("${spring.data.redis.password:}")
    private String password;

    @Value("${spring.data.redis.database:0}")
    private int database;

    @Value("${riverflow.redisson.connection-pool-size:24}")
    private int connectionPoolSize;

    @Value("${riverflow.redisson.connection-minimum-idle-size:8}")
    private int connectionMinimumIdleSize;

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();
        SingleServerConfig serverConfig = config.useSingleServer()
                .setAddress("redis://" + host + ":" + port)
                .setDatabase(database)
                .setConnectionPoolSize(connectionPoolSize)
                .setConnectionMinimumIdleSize(connectionMinimumIdleSize)
                .setIdleConnectionTimeout(10000)
                .setConnectTimeout(10000)
                .setTimeout(3000);

        // 只有密码非空时才设置，避免 Redis 无密码时 Redisson 发送 AUTH 空密码导致连接失败
        if (StringUtils.hasText(password)) {
            serverConfig.setPassword(password);
        }

        return Redisson.create(config);
    }
}
