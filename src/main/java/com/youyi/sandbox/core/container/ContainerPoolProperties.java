package com.youyi.sandbox.core.container;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.concurrent.TimeUnit;

/**
 * @author yoyocraft
 * @date 2024/09/18
 */
@Getter
@Setter
@ConfigurationProperties("code-sandbox.pool")
public class ContainerPoolProperties {

    private volatile int corePoolSize = Runtime.getRuntime().availableProcessors() * 10;

    private volatile int maxPoolSize = Runtime.getRuntime().availableProcessors() * 20;

    private volatile int waitQueueSize = 200;

    private volatile long keepAliveTime = 5000L;

    private volatile TimeUnit timeUnit = TimeUnit.MILLISECONDS;
}
