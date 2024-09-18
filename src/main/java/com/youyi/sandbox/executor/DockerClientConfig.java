package com.youyi.sandbox.executor;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DockerClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author yoyocraft
 * @date 2024/09/18
 */
@Configuration
public class DockerClientConfig {

    @Bean
    public DockerClient dockerClient() {
        return DockerClientBuilder.getInstance().build();
    }
}
