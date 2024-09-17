package com.youyi.sandbox.base;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * @author yoyocraft
 * @date 2024/09/13
 */
@Getter
@AllArgsConstructor
public class PageResult<T> {

    private Integer total;
    private List<T> records;
}
