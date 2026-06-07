package com.riverflow.admin.modules.workflow.engine;

import com.riverflow.admin.service.FlowLogService;
import com.riverflow.api.entity.FlowLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 异步日志保存服务
 * 使用线程池异步保存流程日志，避免阻塞流程执行
 * 
 * 同步流程：不需要重试，调用方可以自己处理
 * 异步流程：需要重试，确保日志不丢失
 */
@Slf4j
@Component
public class AsyncLogService {
    
    private ExecutorService logExecutor;
    
    @Autowired
    private FlowLogService flowLogService;
    
    @PostConstruct
    public void init() {
        logExecutor = Executors.newFixedThreadPool(2, r -> {
            Thread t = new Thread(r, "async-log-saver");
            t.setDaemon(true);
            return t;
        });
        log.info("异步日志服务初始化完成，线程池大小: 2");
    }
    
    @PreDestroy
    public void shutdown() {
        if (logExecutor != null) {
            logExecutor.shutdown();
            try {
                if (!logExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                    logExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                logExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        log.info("异步日志服务已关闭");
    }
    
    /**
     * 异步保存单条日志（同步流程使用，不重试）
     */
    public void saveLogAsync(FlowLog flowLog) {
        if (flowLog == null) return;
        
        logExecutor.submit(() -> {
            try {
                flowLogService.save(flowLog);
                log.debug("异步保存日志成功: instanceId={}, nodeId={}, logType={}", 
                    flowLog.getInstanceId(), flowLog.getNodeId(), flowLog.getLogType());
            } catch (Exception e) {
                log.error("异步保存日志失败: instanceId={}, nodeId={}, error={}", 
                    flowLog.getInstanceId(), flowLog.getNodeId(), e.getMessage());
            }
        });
    }
    
    /**
     * 异步批量保存日志（同步流程使用，不重试）
     */
    public void saveBatchAsync(List<FlowLog> logs) {
        if (logs == null || logs.isEmpty()) return;
        
        logExecutor.submit(() -> {
            try {
                flowLogService.saveBatch(logs);
                log.debug("异步批量保存日志成功: count={}", logs.size());
            } catch (Exception e) {
                log.error("异步批量保存日志失败: count={}, error={}", 
                    logs.size(), e.getMessage());
            }
        });
    }
    
    /**
     * 异步保存日志（异步流程使用，带重试）
     * 异步流程调用方已离开，需要确保日志保存成功
     */
    public void saveLogWithRetry(FlowLog flowLog, int maxRetries) {
        if (flowLog == null) return;
        
        logExecutor.submit(() -> {
            for (int i = 0; i < maxRetries; i++) {
                try {
                    flowLogService.save(flowLog);
                    log.debug("异步保存日志成功（重试机制）: instanceId={}", flowLog.getInstanceId());
                    return;
                } catch (Exception e) {
                    log.warn("异步保存日志失败（第{}次）: instanceId={}, error={}", 
                        i + 1, flowLog.getInstanceId(), e.getMessage());
                    if (i < maxRetries - 1) {
                        try {
                            Thread.sleep(100 * (i + 1));
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }
            log.error("异步保存日志最终失败: instanceId={}", flowLog.getInstanceId());
        });
    }
    
    /**
     * 异步批量保存日志（异步流程使用，带重试）
     */
    public void saveBatchWithRetry(List<FlowLog> logs, int maxRetries) {
        if (logs == null || logs.isEmpty()) return;
        
        logExecutor.submit(() -> {
            for (int i = 0; i < maxRetries; i++) {
                try {
                    flowLogService.saveBatch(logs);
                    log.debug("异步批量保存日志成功（重试机制）: count={}", logs.size());
                    return;
                } catch (Exception e) {
                    log.warn("异步批量保存日志失败（第{}次）: count={}, error={}", 
                        i + 1, logs.size(), e.getMessage());
                    if (i < maxRetries - 1) {
                        try {
                            Thread.sleep(100 * (i + 1));
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }
            log.error("异步批量保存日志最终失败: count={}", logs.size());
        });
    }
}