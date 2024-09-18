package com.youyi.sandbox.core.execuotor;

import com.youyi.sandbox.core.container.ContainerPoolExecutor;
import com.youyi.sandbox.core.dto.resp.ExecutorMessage;
import com.youyi.sandbox.core.enums.LanguageCmdEnum;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * @author yoyocraft
 * @date 2024/09/18
 */
@Component
public class DockerSandbox {

    @Autowired
    private DockerDAO dockerDAO;

    @Autowired
    private ContainerPoolExecutor containerPoolExecutor;

    /**
     * 执行代码
     * @param code 代码
     * @param languageCmdEnum 代码语言
     * @return 执行信息 {@link ExecutorMessage}
     */
    public ExecutorMessage execute(String code, LanguageCmdEnum languageCmdEnum) {
        return containerPoolExecutor.submit(containerInfo -> {

            String containerId = containerInfo.getContainerId();
            String codePath = containerInfo.getCodePath();
            String codeFile = codePath + File.separator + languageCmdEnum.getFileName();

            try {
                FileUtils.writeStringToFile(new File(codeFile), code, StandardCharsets.UTF_8);
                dockerDAO.copyFileToContainer(codeFile, containerId);

                // 编译代码
                String[] compileCmd = languageCmdEnum.getCompileCmd();
                ExecutorMessage executorMessage;

                if (ArrayUtils.isNotEmpty(compileCmd)) {
                    // 需要编译
                    executorMessage = dockerDAO.execCmd(containerId, compileCmd);
                    if (!executorMessage.getSuccess()) {
                        // 编译失败
                        return executorMessage;
                    }
                }
                // 执行代码
                String[] runCmd = languageCmdEnum.getRunCmd();
                executorMessage = dockerDAO.execCmd(containerId, runCmd);
                return executorMessage;
            } catch (Exception e) {
                return ExecutorMessage.builder()
                        .success(Boolean.FALSE)
                        .errorMsg(e.getMessage())
                        .build();
            }
        });
    }
}
