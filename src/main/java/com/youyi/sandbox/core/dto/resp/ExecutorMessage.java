package com.youyi.sandbox.core.dto.resp;

import com.youyi.sandbox.base.BaseResp;
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
public class ExecutorMessage extends BaseResp {

    private Boolean success;
    private String message;
    private String errorMsg;

}
