package com.youyi.sandbox.constants;

/**
 * @author yoyocraft
 * @date 2024/09/13
 */
public class ConfigConstant {

    /**
     * thread pool 相关
     */
    public static final int COMMON_ASYNC_CORE_POOL_SIZE = 10;
    public static final int COMMON_ASYNC_MAX_POOL_SIZE = 20;
    public static final int COMMON_ASYNC_QUEUE_CAPACITY = 100;
    public static final long COMMON_ASYNC_KEEP_ALIVE_SECONDS = 60L;
    public static final String COMMON_ASYNC_THREAD_NAME_FORMAT = "common-async-%s";
}
