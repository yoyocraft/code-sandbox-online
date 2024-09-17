package com.youyi.sandbox.base;

import com.youyi.sandbox.enums.ReturnCodeEnum;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author yoyocraft
 * @date 2024/09/13
 */
@Setter
@Getter
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private T data;
    private Integer code;
    private String message;

    private Result(ReturnCodeEnum returnCodeEnum) {
        this.data = null;
        this.code = returnCodeEnum.getCode();
        this.message = returnCodeEnum.getDesc();
    }

    private Result(T data, ReturnCodeEnum returnCodeEnum) {
        this.data = data;
        this.code = returnCodeEnum.getCode();
        this.message = returnCodeEnum.getDesc();
    }

    private Result(Integer code, String message) {
        this.data = null;
        this.code = code;
        this.message = message;
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(data, ReturnCodeEnum.SUCCESS);
    }

    public static <T> Result<T> fail(ReturnCodeEnum codeEnum) {
        codeEnum = codeEnum == null ? ReturnCodeEnum.SYSTEM_ERROR : codeEnum;
        return new Result<>(codeEnum);
    }

    public static <T> Result<T> fail(String message) {
        return new Result<>(ReturnCodeEnum.SYSTEM_ERROR.getCode(), message);
    }

}
