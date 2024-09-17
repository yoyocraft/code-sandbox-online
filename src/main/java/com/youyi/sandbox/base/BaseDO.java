package com.youyi.sandbox.base;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author yoyocraft
 * @date 2024/09/13
 */
@Data
public class BaseDO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private Date createTime;

    private Date updateTime;

    private Integer deleted;
}
