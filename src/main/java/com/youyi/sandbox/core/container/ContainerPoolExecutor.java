package com.youyi.sandbox.core.container;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.youyi.sandbox.exception.BusinessException;
import com.youyi.sandbox.core.execuotor.DockerDAO;
import com.youyi.sandbox.core.dto.resp.ExecutorMessage;
import com.youyi.sandbox.utils.UUIDUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * @author yoyocraft
 * @date 2024/09/18
 */
@Component
public class ContainerPoolExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ContainerPoolExecutor.class);

    private static final ExecutorService PUT_BACK_POOL_EXECUTOR = new ThreadPoolExecutor(
            2,
            4,
            60,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(100),
            new ThreadFactoryBuilder().setNameFormat("put-back-pool-%d").build(),
            new ThreadPoolExecutor.CallerRunsPolicy()
    );

    /**
     * 容器池
     */
    private BlockingQueue<ContainerInfo> containerPool;

    /**
     * 阻塞等待线程数量
     */
    private AtomicInteger blockingThreadCnt;

    /**
     * 可扩展容器的数量
     */
    private AtomicInteger expandCount;

    private final ContainerPoolProperties containerPoolProperties;

    @Autowired
    private DockerDAO dockerDAO;

    public ContainerPoolExecutor(ContainerPoolProperties containerPoolProperties) {
        this.containerPoolProperties = containerPoolProperties;
    }

    @PostConstruct
    public void init() {
        final int corePoolSize = containerPoolProperties.getCorePoolSize();
        final int maxPoolSize = containerPoolProperties.getMaxPoolSize();
        this.containerPool = new LinkedBlockingQueue<>(maxPoolSize);
        this.blockingThreadCnt = new AtomicInteger(0);
        this.expandCount = new AtomicInteger(maxPoolSize - corePoolSize);

        for (int i = 0; i < corePoolSize; i++) {
            ContainerInfo containerInfo = createContainer();
            if (!containerPool.offer(containerInfo)) {
                LOGGER.error("current capacity: {}, this capacity limit is exceeded!", containerPool.size());
            }
        }

        // 定时任务，清理过期的容器
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduleExpirationCleanup(scheduledExecutorService);
    }

    public ExecutorMessage submit(Function<ContainerInfo, ExecutorMessage> task) {
        ContainerInfo containerInfo = null;

        try {
            containerInfo = getContainer();
            if (containerInfo == null) {
                return ExecutorMessage.builder()
                        .success(Boolean.FALSE)
                        .message("no available container")
                        .build();
            }

            ExecutorMessage executorMessage = task.apply(containerInfo);
            if (!executorMessage.getSuccess()) {
                recordError(containerInfo);
            }
            return executorMessage;
        } catch (InterruptedException e) {
            throw new BusinessException("submit task failed, " + e.getMessage());
        } finally {
            if (containerInfo != null) {
                final ContainerInfo container = containerInfo;
                dockerDAO.execCmd(containerInfo.getContainerId(), new String[]{"rm", "-rf", "/box"});
                CompletableFuture.runAsync(() -> {
                    try {
                        String codePath = container.getCodePath();
                        FileUtils.delete(new File(codePath));
                        if (container.getErrorCnt() > 3) {
                            dockerDAO.cleanContainer(container.getContainerId());
                            containerPool.offer(createContainer());
                            return;
                        }
                        container.setLastActivityTime(System.currentTimeMillis());
                        containerPool.put(container);
                    } catch (IOException | InterruptedException e) {
                        LOGGER.error("cannot put back container: {}, pool size: {}", container.getContainerId(), containerPool.size());
                    }
                }, PUT_BACK_POOL_EXECUTOR);
            }
        }
    }

    public ContainerInfo getContainer() throws InterruptedException {
        if (containerPool.isEmpty()) {
            try {
                if (blockingThreadCnt.incrementAndGet() >= containerPoolProperties.getWaitQueueSize() && !expandPool()) {
                    LOGGER.warn("expand failed!");
                    return null;
                }
                LOGGER.warn("waiting container, wait size: {}", blockingThreadCnt.get());
                return containerPool.take();
            } finally {
                blockingThreadCnt.decrementAndGet();
            }
        }
        return containerPool.take();
    }

    public void cleanContainerPool() {
        containerPool
                .forEach(containerInfo -> {
                    try {
                        FileUtils.delete(new File(containerInfo.getCodePath()));
                        dockerDAO.cleanContainer(containerInfo.getContainerId());
                    } catch (IOException e) {
                        LOGGER.error("delete container failed, containerId: {}", containerInfo.getContainerId(), e);
                    }
                });
    }

    private ContainerInfo createContainer() {
        String userDir = System.getProperty("user.dir");
        String codePath = userDir + File.separator + "tmp-codes" + File.separator + UUIDUtil.getUUID();

        File codePathFile = new File(codePath);
        if (!codePathFile.exists()) {
            synchronized (this) {
                if (!codePathFile.exists() && !codePathFile.mkdirs()) {
                    LOGGER.error("create code path failed");
                }
            }

        }
        return dockerDAO.createContainer(codePath);
    }

    private void scheduleExpirationCleanup(ScheduledExecutorService executor) {
        executor.scheduleAtFixedRate(this::cleanExpiredContainer, 0, 20, TimeUnit.SECONDS);
    }

    private void cleanExpiredContainer() {
        long now = System.currentTimeMillis();
        int needCleanCnt = containerPool.size() - containerPoolProperties.getCorePoolSize();
        if (needCleanCnt <= 0) {
            return;
        }
        LOGGER.info("clean expired container, cnt: {}", needCleanCnt);

        containerPool.stream()
                .filter(containerInfo -> {
                    long lastActivityTime = containerInfo.getLastActivityTime();
                    lastActivityTime += containerPoolProperties.getTimeUnit().toMillis(containerPoolProperties.getKeepAliveTime());
                    return lastActivityTime < now;
                })
                .forEach(containerInfo -> {
                    boolean remove = containerPool.remove(containerInfo);
                    if (remove) {
                        String containerId = containerInfo.getContainerId();
                        expandCount.incrementAndGet();
                        if (StringUtils.isNoneBlank(containerId)) {
                            dockerDAO.cleanContainer(containerId);
                        }
                    }
                });
    }

    private boolean expandPool() {
        LOGGER.warn("expand container pool");
        if (expandCount.decrementAndGet() < 0) {
            LOGGER.error("cannot expand!");
            return false;
        }
        return containerPool.offer(createContainer());
    }

    private void recordError(ContainerInfo containerInfo) {
        if (containerInfo == null) {
            return;
        }
        containerInfo.setErrorCnt(containerInfo.getErrorCnt() + 1);
    }

}
