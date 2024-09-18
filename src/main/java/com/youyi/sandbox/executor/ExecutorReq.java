package com.youyi.sandbox.executor;

import com.youyi.sandbox.base.BaseReq;
import lombok.Getter;
import lombok.Setter;

/**
 * @author yoyocraft
 * @date 2024/09/17
 */
@Getter
@Setter
public class ExecutorReq extends BaseReq {

    private String language;
    private String code;
}
