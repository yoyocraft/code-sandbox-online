package com.youyi.sandbox.executor;

import com.alibaba.fastjson2.JSON;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.StreamType;
import com.youyi.sandbox.enums.LanguageCmdEnum;
import com.youyi.sandbox.exception.BusinessException;
import com.youyi.sandbox.utils.UUIDUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author yoyocraft
 * @date 2024/09/18
 */
@Component
public class DockerSandbox {

    private static final Logger LOGGER = LoggerFactory.getLogger(DockerSandbox.class);
    private final DockerClient dockerClient;

    @Autowired
    private ContainerProperties containerProperties;

    public DockerSandbox(DockerClient dockerClient) {
        this.dockerClient = dockerClient;
    }

    /**
     * 执行代码
     * @param code 代码
     * @param languageCmdEnum 代码语言
     * @return 执行信息 {@link ExecutorMessage}
     */
    public ExecutorMessage execute(String code, LanguageCmdEnum languageCmdEnum) throws IOException {
        // 1. 写入文件
        String userDir = System.getProperty("user.dir");
        String language = languageCmdEnum.getLanguage();
        String globalCodePath = userDir + File.separator + "tmp-codes" + File.separator + language;
        File globalCodePathFile = new File(globalCodePath);
        // TODO youyi 2024/9/18 考虑加锁
        if (!globalCodePathFile.exists()) {
            boolean mkdir = globalCodePathFile.mkdirs();
            if (!mkdir) {
                throw new BusinessException("create global code path failed");
            }
        }
        String userCodeParentPath = globalCodePath + File.separator + UUIDUtil.getUUID();
        String codeFile = userCodeParentPath + File.separator + languageCmdEnum.getFileName();
        FileUtils.writeStringToFile(new File(codeFile), code, StandardCharsets.UTF_8);

        // 2. 创建容器
        String containerId = createContainer(codeFile);

        // 3. 编译代码
        String[] compileCmd = languageCmdEnum.getCompileCmd();
        ExecutorMessage executorMessage;
        if (ArrayUtils.isNotEmpty(compileCmd)) {
            // 不为空，则编译
            executorMessage = execCmd(containerId, compileCmd);
            LOGGER.debug("execCmd compileCmd result: {}", JSON.toJSONString(executorMessage));
            if (!executorMessage.getSuccess()) {
                // 编译失败
                cleanFileAndContainer(containerId, userCodeParentPath);
                return executorMessage;
            }
        }
        // 4. 执行代码
        executorMessage = execCmd(containerId, languageCmdEnum.getRunCmd());
        LOGGER.debug("execCmd runCmd result: {}", JSON.toJSONString(executorMessage));
        // 5. 清理文件
        cleanFileAndContainer(containerId, userCodeParentPath);
        // 6. 返回结果
        return executorMessage;
    }

    /**
     * 创建容器
     * @param codeFile 源代码文件
     * @return 容器ID
     */
    private String createContainer(String codeFile) {
        CreateContainerCmd containerCmd = dockerClient.createContainerCmd(containerProperties.getImage());

        HostConfig hostConfig = new HostConfig();
        hostConfig.withMemory(containerProperties.getMemoryLimit());
        hostConfig.withMemorySwap(containerProperties.getMemorySwap());
        hostConfig.withCpuCount(containerProperties.getCpuCount());

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

        // 将代码文件复制到容器中
        dockerClient.copyArchiveToContainerCmd(containerId)
                .withHostResource(codeFile)
                .withRemotePath("/box")
                .exec();

        return containerId;
    }

    /**
     * 执行命令
     * @param containerId 容器ID
     * @param cmd 执行的命令
     * @return 执行结果 {@link ExecutorMessage}
     */
    private ExecutorMessage execCmd(String containerId, String[] cmd) {
        // 正常返回信息
        ByteArrayOutputStream resultStream = new ByteArrayOutputStream();
        // 错误返回信息
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();

        // 结果
        final boolean[] result = new boolean[1];

        try (ResultCallback.Adapter<Frame> frameAdapter = new ResultCallback.Adapter<Frame>() {
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
     * 清理文件和容器
     * @param containerId 容器ID
     * @param codePath 代码路径
     */
    private void cleanFileAndContainer(String containerId, String codePath) throws IOException {
        if (StringUtils.isBlank(codePath)) {
            LOGGER.warn("codePath is blank, containerId: {}", containerId);
            return;
        }
        // 清理临时目录
        FileUtils.deleteDirectory(new File(codePath));

        // 关闭并删除容器
        dockerClient.stopContainerCmd(containerId).exec();
        dockerClient.removeContainerCmd(containerId).exec();
    }
}
