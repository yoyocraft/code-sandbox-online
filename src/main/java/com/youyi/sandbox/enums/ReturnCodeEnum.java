package com.youyi.sandbox.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author yoyocraft
 * @date 2024/09/13
 */
@Getter
@AllArgsConstructor
public enum ReturnCodeEnum {
    SUCCESS(200, "成功"),
    SYSTEM_ERROR(500, "系统异常"),
    REQUEST_LATER(429, "请稍后再重试"),
    ;

    private final int code;
    private final String desc;
}
