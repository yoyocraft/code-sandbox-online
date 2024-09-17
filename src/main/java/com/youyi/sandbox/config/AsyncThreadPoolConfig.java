package com.youyi.sandbox.config;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.youyi.sandbox.constants.ConfigConstant;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author yoyocraft
 * @date 2024/09/13
 */
@Configuration
public class AsyncThreadPoolConfig {

    @Bean
    public ExecutorService commonAsyncThreadPool() {
        return new ThreadPoolExecutor(
                ConfigConstant.COMMON_ASYNC_CORE_POOL_SIZE,
                ConfigConstant.COMMON_ASYNC_MAX_POOL_SIZE,
                ConfigConstant.COMMON_ASYNC_KEEP_ALIVE_SECONDS,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(ConfigConstant.COMMON_ASYNC_QUEUE_CAPACITY),
                new ThreadFactoryBuilder().setNameFormat(ConfigConstant.COMMON_ASYNC_THREAD_NAME_FORMAT).build(),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }
}
