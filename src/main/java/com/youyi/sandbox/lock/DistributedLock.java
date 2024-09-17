package com.youyi.sandbox.lock;

import java.util.concurrent.TimeUnit;

/**
 * @author yoyocraft
 * @date 2024/09/13
 */
public interface DistributedLock {

    Boolean lock(String key, int expire, TimeUnit timeUnit);

    Boolean unlock(String key);
}
