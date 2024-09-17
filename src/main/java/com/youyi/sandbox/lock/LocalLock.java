package com.youyi.sandbox.lock;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 本地 Lock 实现
 * @author yoyocraft
 * @date 2024/09/13
 */
@Component
public class LocalLock implements DistributedLock {

    private static final Object PRESENT = new Object();

    private final Cache<String, Object> lockCache = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .build();

    @Override
    public Boolean lock(String key, int expire, TimeUnit timeUnit) {
        if (lockCache.getIfPresent(key) == null) {
            lockCache.put(key, PRESENT);
            return true;
        }
        return false;
    }

    @Override
    public Boolean unlock(String key) {
        lockCache.invalidate(key);
        return true;
    }
}
