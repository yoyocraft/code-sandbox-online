package com.youyi.sandbox.core.dto.req;

import com.youyi.sandbox.base.BaseReq;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

/**
 * @author yoyocraft
 * @date 2024/09/17
 */
@Getter
@Setter
public class ExecutorReq extends BaseReq {

    @NotNull
    private String language;

    @NotNull
    private String code;
}
