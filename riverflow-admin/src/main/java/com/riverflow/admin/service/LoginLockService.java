package com.riverflow.admin.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 登录账号锁定服务
 * <p>
 * 基于 Redis 记录连续登录失败次数，超过阈值后锁定账号一段时间。
 */
@Service
public class LoginLockService {

    private static final String FAIL_COUNT_KEY_PREFIX = "riverflow:login:fail:";
    private static final String LOCK_KEY_PREFIX = "riverflow:login:lock:";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Value("${login.lock.max-fail-times:5}")
    private int maxFailTimes;

    @Value("${login.lock.lock-minutes:30}")
    private int lockMinutes;

    /**
     * 检查账号是否已被锁定
     *
     * @param username 用户名
     * @return 锁定剩余秒数，未锁定返回 0
     */
    public long checkLocked(String username) {
        String lockKey = LOCK_KEY_PREFIX + username;
        String ttlStr = stringRedisTemplate.opsForValue().get(lockKey);
        if (ttlStr == null) {
            return 0;
        }
        Long ttl = stringRedisTemplate.getExpire(lockKey, TimeUnit.SECONDS);
        return ttl == null ? 0 : Math.max(ttl, 1);
    }

    /**
     * 记录一次登录失败
     *
     * @param username 用户名
     * @return 失败次数
     */
    public int recordFailure(String username) {
        String failKey = FAIL_COUNT_KEY_PREFIX + username;
        Long count = stringRedisTemplate.opsForValue().increment(failKey);
        if (count == null) {
            return 1;
        }
        // 失败次数有效期与锁定窗口保持一致
        stringRedisTemplate.expire(failKey, Duration.ofMinutes(lockMinutes));

        if (count >= maxFailTimes) {
            lock(username);
            stringRedisTemplate.delete(failKey);
        }
        return count.intValue();
    }

    /**
     * 登录成功后清除失败记录
     *
     * @param username 用户名
     */
    public void clearFailure(String username) {
        stringRedisTemplate.delete(FAIL_COUNT_KEY_PREFIX + username);
    }

    /**
     * 锁定账号
     *
     * @param username 用户名
     */
    public void lock(String username) {
        stringRedisTemplate.opsForValue().set(
                LOCK_KEY_PREFIX + username,
                "1",
                Duration.ofMinutes(lockMinutes)
        );
    }

    /**
     * 获取剩余可失败次数
     *
     * @param username 用户名
     * @return 剩余次数
     */
    public int getRemainingAttempts(String username) {
        // 账号已被锁定时，剩余次数为 0
        if (checkLocked(username) > 0) {
            return 0;
        }
        String failKey = FAIL_COUNT_KEY_PREFIX + username;
        String countStr = stringRedisTemplate.opsForValue().get(failKey);
        if (countStr == null) {
            return maxFailTimes;
        }
        try {
            int count = Integer.parseInt(countStr);
            return Math.max(maxFailTimes - count, 0);
        } catch (NumberFormatException e) {
            return maxFailTimes;
        }
    }
}
