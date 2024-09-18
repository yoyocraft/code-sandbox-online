package com.youyi.sandbox.core.cleaner;

import com.youyi.sandbox.core.container.ContainerPoolExecutor;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

/**
 * @author yoyocraft
 * @date 2024/09/18
 */
@Component
public class CleanContainerListener implements ApplicationListener<ContextClosedEvent> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CleanContainerListener.class);

    @Autowired
    private ContainerPoolExecutor containerPoolExecutor;

    @Override
    public void onApplicationEvent(@NonNull ContextClosedEvent event) {
        LOGGER.info("context closing, clean container pool");
        containerPoolExecutor.cleanContainerPool();
    }
}
