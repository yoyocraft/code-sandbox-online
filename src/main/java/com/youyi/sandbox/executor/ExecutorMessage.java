package com.youyi.sandbox.executor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author yoyocraft
 * @date 2024/09/17
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExecutorMessage {

    private Boolean success;
    private String message;
    private String errorMsg;

}
