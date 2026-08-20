package com.riverflow.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.riverflow.admin.mapper.ApiCallLogMapper;
import com.riverflow.admin.service.ApiCallLogService;
import com.riverflow.api.entity.ApiCallLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 接口调用日志 Service 实现
 */
@Slf4j
@Service
public class ApiCallLogServiceImpl extends ServiceImpl<ApiCallLogMapper, ApiCallLog> implements ApiCallLogService {

    /**
     * 日志写入线程池：单线程 + 有界队列，队列满时丢弃日志并打印错误，绝不阻塞接口主流程
     */
    private static final ExecutorService LOG_EXECUTOR = new ThreadPoolExecutor(
            1,
            2,
            60L,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1000),
            new ThreadFactory() {
                private final AtomicInteger counter = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "api-call-log-" + counter.incrementAndGet());
                    t.setDaemon(true);
                    return t;
                }
            },
            new ThreadPoolExecutor.DiscardPolicy()
    );

    @Override
    public void saveAsync(ApiCallLog callLog) {
        try {
            LOG_EXECUTOR.submit(() -> {
                try {
                    save(callLog);
                } catch (Exception e) {
                    log.error("保存接口调用日志失败: apiCode={}, error={}", callLog.getApiCode(), e.getMessage());
                }
            });
        } catch (Exception e) {
            log.error("提交接口调用日志任务失败: {}", e.getMessage());
        }
    }
}
