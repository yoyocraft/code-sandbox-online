package com.youyi.sandbox.core.container;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * 容器信息
 * @author yoyocraft
 * @date 2024/09/18
 */
@Getter
@Setter
@Builder
public class ContainerInfo {

    /**
     * 容器的唯一标识
     */
    private String containerId;

    /**
     * 宿主机临时代码存储文件位置
     */
    private String codePath;

    /**
     * 容器上次活跃时间
     */
    private long lastActivityTime;

    /**
     * 容器执行失败次数
     */
    private int errorCnt;
}
