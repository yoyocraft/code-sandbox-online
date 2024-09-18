package com.youyi.sandbox.core.execuotor;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.StreamType;
import com.youyi.sandbox.core.container.ContainerInfo;
import com.youyi.sandbox.core.container.ContainerProperties;
import com.youyi.sandbox.core.dto.resp.ExecutorMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author yoyocraft
 * @date 2024/09/18
 */
@Component
public class DockerDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(DockerDAO.class);

    private final DockerClient dockerClient;
    private final ContainerProperties containerProperties;

    public DockerDAO(DockerClient dockerClient, ContainerProperties containerProperties) {
        this.dockerClient = dockerClient;
        this.containerProperties = containerProperties;
    }

    /**
     * 创建容器
     * @param codeFilePath 源代码文件路径
     * @return 容器信息
     */
    public ContainerInfo createContainer(String codeFilePath) {
        CreateContainerCmd containerCmd = dockerClient.createContainerCmd(containerProperties.getImage());

        HostConfig hostConfig = new HostConfig();
        hostConfig.withMemory(containerProperties.getMemoryLimit());
        hostConfig.withMemorySwap(containerProperties.getMemorySwap());
        hostConfig.withCpuCount(containerProperties.getCpuCount());
        hostConfig.withReadonlyRootfs(Boolean.TRUE);

        // 创建容器
        CreateContainerResponse createContainerResponse = containerCmd
                .withHostConfig(hostConfig)
                .withNetworkDisabled(Boolean.TRUE)
                .withAttachStdin(Boolean.TRUE)
                .withAttachStderr(Boolean.TRUE)
                .withAttachStdout(Boolean.TRUE)
                .withTty(Boolean.TRUE)
                .exec();
        String containerId = createContainerResponse.getId();

        // 启动容器
        dockerClient.startContainerCmd(containerId).exec();

        return ContainerInfo.builder()
                .containerId(containerId)
                .codePath(codeFilePath)
                .lastActivityTime(System.currentTimeMillis())
                .build();
    }

    /**
     * 将文件拷贝到容器中
     * @param codeFile 源代码文件
     * @param containerId 容器唯一标识
     */
    public void copyFileToContainer(String codeFile, String containerId) {
        dockerClient.copyArchiveToContainerCmd(containerId)
                .withHostResource(codeFile)
                .withRemotePath("/box")
                .exec();
    }

    /**
     * 执行命令
     * @param containerId 容器ID
     * @param cmd 执行的命令
     * @return 执行结果 {@link ExecutorMessage}
     */
    public ExecutorMessage execCmd(String containerId, String[] cmd) {
        // 正常返回信息
        ByteArrayOutputStream resultStream = new ByteArrayOutputStream();
        // 错误返回信息
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();

        // 结果
        final boolean[] result = new boolean[]{true};
        // 超时
        final boolean[] timeout = new boolean[]{true};

        try (ResultCallback.Adapter<Frame> frameAdapter = new ResultCallback.Adapter<Frame>() {

            @Override
            public void onComplete() {
                // 不超时
                timeout[0] = false;
                super.onComplete();
            }

            @Override
            public void onNext(Frame frame) {
                StreamType streamType = frame.getStreamType();
                byte[] payload = frame.getPayload();
                if (StreamType.STDERR.equals(streamType)) {
                    try {
                        result[0] = false;
                        errorStream.write(payload);
                    } catch (IOException e) {
                        LOGGER.info("execCmd errorStream write error", e);
                    }
                } else {
                    try {
                        result[0] = true;
                        resultStream.write(payload);
                    } catch (IOException e) {
                        LOGGER.info("execCmd resultStream write error", e);
                    }
                }
                super.onNext(frame);
            }
        }) {
            ExecCreateCmdResponse execCompileCmdResponse = dockerClient.execCreateCmd(containerId)
                    .withCmd(cmd)
                    .withAttachStdin(Boolean.TRUE)
                    .withAttachStdout(Boolean.TRUE)
                    .withAttachStderr(Boolean.TRUE)
                    .exec();
            String execId = execCompileCmdResponse.getId();
            dockerClient
                    .execStartCmd(execId)
                    .exec(frameAdapter)
                    .awaitCompletion(containerProperties.getTimeout(), containerProperties.getTimeUnit());

            if (timeout[0]) {
                // 超时
                return ExecutorMessage.builder()
                        .success(Boolean.FALSE)
                        .errorMsg("timeout")
                        .build();
            }

            return ExecutorMessage.builder()
                    .success(result[0])
                    .message(resultStream.toString())
                    .errorMsg(errorStream.toString())
                    .build();
        } catch (IOException | InterruptedException e) {
            LOGGER.info("execCmd error", e);
            return ExecutorMessage.builder()
                    .success(Boolean.FALSE)
                    .errorMsg(e.getMessage())
                    .build();
        }
    }

    /**
     * 关闭并删除容器
     * @param containerId 容器唯一标识
     */
    public void cleanContainer(String containerId) {
        dockerClient.stopContainerCmd(containerId).exec();
        dockerClient.removeContainerCmd(containerId).exec();
    }
}
