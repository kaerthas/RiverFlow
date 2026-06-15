package com.riverflow.admin.service;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 登录账号锁定服务测试
 */
@SpringBootTest
@ActiveProfiles("test")
public class LoginLockServiceTest {

    private static final Logger log = LoggerFactory.getLogger(LoginLockServiceTest.class);

    @Autowired
    private LoginLockService loginLockService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String TEST_USER = "test_lock_user";

    @Test
    public void testLockAndClear() {
        String failKey = "riverflow:login:fail:" + TEST_USER;
        String lockKey = "riverflow:login:lock:" + TEST_USER;

        log.info("当前 Redis database: {}", stringRedisTemplate.getConnectionFactory().getConnection().getClientName());

        // 清理历史数据
        Boolean delFail = stringRedisTemplate.delete(failKey);
        Boolean delLock = stringRedisTemplate.delete(lockKey);
        log.info("清理历史 key: failKey={}, lockKey={}", delFail, delLock);
        log.info("清理后 lockKey 值: {}, ttl: {}",
                stringRedisTemplate.opsForValue().get(lockKey),
                stringRedisTemplate.getExpire(lockKey));

        // 初始未锁定
        assertEquals(0, loginLockService.checkLocked(TEST_USER));
        assertEquals(5, loginLockService.getRemainingAttempts(TEST_USER));

        // 连续失败 4 次
        for (int i = 1; i <= 4; i++) {
            int count = loginLockService.recordFailure(TEST_USER);
            assertEquals(i, count);
        }
        assertEquals(1, loginLockService.getRemainingAttempts(TEST_USER));
        assertEquals(0, loginLockService.checkLocked(TEST_USER));

        // 第 5 次失败，触发锁定
        int count = loginLockService.recordFailure(TEST_USER);
        assertEquals(5, count);
        long locked = loginLockService.checkLocked(TEST_USER);
        log.info("锁定后剩余秒数: {}", locked);
        assertTrue(locked > 0);
        assertEquals(0, loginLockService.getRemainingAttempts(TEST_USER));

        // 登录成功后清除失败记录
        loginLockService.clearFailure(TEST_USER);
        // 锁定仍然存在
        assertTrue(loginLockService.checkLocked(TEST_USER) > 0);

        // 手动解锁
        Boolean deleted = stringRedisTemplate.delete(lockKey);
        log.info("删除锁定 key 结果: {}, key={}", deleted, lockKey);
        log.info("删除后 key 值: {}", stringRedisTemplate.opsForValue().get(lockKey));
        log.info("删除后 ttl: {}", stringRedisTemplate.getExpire(lockKey));
        assertEquals(0, loginLockService.checkLocked(TEST_USER));
    }
}
