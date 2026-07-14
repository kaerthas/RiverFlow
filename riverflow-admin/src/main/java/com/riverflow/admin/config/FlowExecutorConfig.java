package com.riverflow.admin.config;

import com.riverflow.common.spring.SpringContextHolder;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;

/**
 * 流程执行线程池配置
 * <p>FlowScheduler 异步提交流程任务时使用。JDK 21 起切换为虚拟线程执行器，
 * 通过 {@link SemaphoreExecutorService} 限制最大并发数，避免无界虚拟线程压垮
 * 数据库连接池、HTTP 连接池等外部资源。
 */
@Slf4j
@Configuration
public class FlowExecutorConfig {

    /**
     * Spring 上下文持有者
     * 供插件等非 Spring 管理类获取 Bean
     */
    @Bean
    public SpringContextHolder springContextHolder() {
        return new SpringContextHolder();
    }

    /**
     * 流程任务执行线程池（虚拟线程 + 信号量限流）
     *
     * <p>使用虚拟线程后，任务阻塞在 I/O 时不会占用操作系统线程，可支撑更高并发；
     * 信号量用于保护外部资源，默认最大并发 30，可通过配置调整：
     * {@code riverflow.virtual-thread.flow-executor-max-concurrency}。
     *
     * <p>建议取值：不超过数据库连接池 max-active 的 1~2 倍，且不超过 HTTP 连接池
     * 可承受的单节点并发量。
     */
    @Bean(name = "flowExecutor", destroyMethod = "shutdown")
    public ExecutorService flowExecutor(
            @Value("${riverflow.virtual-thread.flow-executor-max-concurrency:30}") int maxConcurrency,
            MeterRegistry meterRegistry) {
        log.info("初始化流程任务虚拟线程执行器，最大并发数: {}", maxConcurrency);
        return new SemaphoreExecutorService(maxConcurrency, meterRegistry);
    }
}
