package com.youyi.sandbox.utils;

import java.util.UUID;

/**
 * @author yoyocraft
 * @date 2024/09/18
 */
public class UUIDUtil {
    public static String getUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
