package com.youyi.sandbox.base;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author yoyocraft
 * @date 2024/09/13
 */
@Getter
@Setter
public class BaseReq implements Serializable {
    private static final long serialVersionUID = 1L;

    private String uuid;
}
