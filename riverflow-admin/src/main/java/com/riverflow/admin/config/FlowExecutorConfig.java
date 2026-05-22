package com.riverflow.admin.config;

import com.riverflow.common.spring.SpringContextHolder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 流程执行线程池配置
 * 用于 FlowScheduler 异步提交流程任务，避免单线程顺序阻塞
 */
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
     * 流程任务执行线程池
     * 核心线程4，最大16，队列200，拒绝策略为调用者执行
     */
    @Bean(name = "flowExecutor")
    public ExecutorService flowExecutor() {
        return new ThreadPoolExecutor(
                4,
                16,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(200),
                new ThreadFactory() {
                    private final AtomicInteger counter = new AtomicInteger(0);

                    @Override
                    public Thread newThread(Runnable r) {
                        Thread t = new Thread(r, "flow-worker-" + counter.incrementAndGet());
                        t.setDaemon(true);
                        return t;
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }
}
