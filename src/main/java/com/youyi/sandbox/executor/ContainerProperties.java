package com.youyi.sandbox.executor;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author yoyocraft
 * @date 2024/09/18
 */
@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "code-sandbox:config")
public class ContainerProperties {

    /**
     * 镜像名称
     */
    private String image = "code-sandbox:latest";

    /**
     * 内存相关
     */
    private long memoryLimit = 60L * 1024 * 1024;
    private long memorySwap = 0L;

    /**
     * CPU相关
     */
    private long cpuCount = 1L;

    /**
     * 容器超时时间
     */
    private long timeout = 5000L;
    private TimeUnit timeUnit = TimeUnit.MILLISECONDS;

}
