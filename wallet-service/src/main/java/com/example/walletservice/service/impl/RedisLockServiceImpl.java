package com.example.walletservice.service.impl;

import com.example.walletservice.service.RedisLockService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisLockServiceImpl implements RedisLockService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final Logger logger = LoggerFactory.getLogger(RedisLockServiceImpl.class);

    @Value("${feature.redis.enabled:true}")
    private boolean redisEnabled;

    private static final String LOCK_PREFIX = "lock:wallet:";

    public boolean acquireLockWithTimeout(Long walletId, long timeoutSeconds) {
        logger.info("DistributedLocking : acquireLockWithTimeout, walletId is {}", walletId);

        if (!redisEnabled) {
            logger.warn("DistributedLocking : Redis disabled by feature flag, lock bypassed for walletId: {}", walletId);
            return true;
        }

        String lockKey = LOCK_PREFIX + walletId;
        logger.info("DistributedLocking : acquireLockWithTimeout, lockKey is {}", lockKey);

        try {
            Boolean acquired = redisTemplate.opsForValue()
                    .setIfAbsent(lockKey, "LOCKED", timeoutSeconds, TimeUnit.SECONDS);
            logger.info("DistributedLocking : acquireLockWithTimeout, acquired is {}", acquired);
            return Boolean.TRUE.equals(acquired);
        } catch (Exception ex) {
            logger.warn("DistributedLocking : Redis unavailable, lock bypassed for walletId: {}", walletId, ex);
            return true;
        }
    }

    public void releaseLock(Long walletId) {
        logger.info("DistributedLocking : releaseLock, walletId is {}", walletId);

        if (!redisEnabled) {
            logger.warn("DistributedLocking : Redis disabled by feature flag, release skipped for walletId: {}", walletId);
            return;
        }

        String lockKey = LOCK_PREFIX + walletId;
        logger.info("DistributedLocking : releaseLock, lockKey is {}", lockKey);

        try {
            redisTemplate.delete(lockKey);
        } catch (Exception ex) {
            logger.warn("DistributedLocking : Redis unavailable, release skipped for walletId: {}", walletId, ex);
        }
    }

}
