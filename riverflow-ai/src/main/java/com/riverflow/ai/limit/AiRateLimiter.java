package com.riverflow.ai.limit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * AI 调用限流器
 *
 * <p>基于滑动窗口实现单用户和全局 QPS 限制。
 */
@Slf4j
@Component
public class AiRateLimiter {

    private final Map<String, WindowCounter> userWindows = new ConcurrentHashMap<>();
    private final WindowCounter globalWindow = new WindowCounter();

    /**
     * 默认：单用户每分钟 30 次，全局每分钟 300 次
     */
    private int userMaxRequests = 30;
    private int globalMaxRequests = 300;
    private int windowSeconds = 60;

    /**
     * 尝试获取许可
     *
     * @param userId 用户标识
     * @return true 表示允许调用
     */
    public boolean tryAcquire(String userId) {
        long now = Instant.now().getEpochSecond();
        cleanExpiredWindows(now);

        // 全局限流
        if (!globalWindow.tryAcquire(now, windowSeconds, globalMaxRequests)) {
            log.warn("AI 调用触发全局限流");
            return false;
        }

        // 用户限流
        WindowCounter userCounter = userWindows.computeIfAbsent(userId, k -> new WindowCounter());
        if (!userCounter.tryAcquire(now, windowSeconds, userMaxRequests)) {
            log.warn("AI 调用触发用户限流: userId={}", userId);
            return false;
        }

        return true;
    }

    private void cleanExpiredWindows(long now) {
        long threshold = now - windowSeconds;
        userWindows.entrySet().removeIf(entry -> entry.getValue().getLastSecond() < threshold);
    }

    public void setUserMaxRequests(int userMaxRequests) {
        this.userMaxRequests = userMaxRequests;
    }

    public void setGlobalMaxRequests(int globalMaxRequests) {
        this.globalMaxRequests = globalMaxRequests;
    }

    public void setWindowSeconds(int windowSeconds) {
        this.windowSeconds = windowSeconds;
    }

    /**
     * 滑动窗口计数器
     */
    private static class WindowCounter {
        private final Map<Long, AtomicInteger> seconds = new ConcurrentHashMap<>();
        private long lastSecond = 0;

        boolean tryAcquire(long second, int windowSize, int maxRequests) {
            lastSecond = second;
            AtomicInteger count = seconds.computeIfAbsent(second, k -> new AtomicInteger(0));
            // 计算当前窗口内总请求数
            long start = second - windowSize + 1;
            int total = seconds.entrySet().stream()
                    .filter(e -> e.getKey() >= start && e.getKey() <= second)
                    .mapToInt(e -> e.getValue().get())
                    .sum();
            if (total >= maxRequests) {
                return false;
            }
            count.incrementAndGet();
            return true;
        }

        long getLastSecond() {
            return lastSecond;
        }
    }
}
