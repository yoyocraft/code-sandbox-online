package com.youyi.sandbox.controller;

import com.youyi.sandbox.base.Result;
import com.youyi.sandbox.core.dto.req.ExecutorReq;
import com.youyi.sandbox.core.dto.resp.ExecutorMessage;
import com.youyi.sandbox.core.enums.LanguageCmdEnum;
import com.youyi.sandbox.core.execuotor.DockerSandbox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yoyocraft
 * @date 2024/09/18
 */
@RestController
@RequestMapping("/oj")
public class ExecuteController {

    @Autowired
    private DockerSandbox dockerSandbox;

    @PostMapping("/exec")
    public Result<ExecutorMessage> exec(@RequestBody ExecutorReq req) {
        // TODO youyi 2024/9/18 auth
        return Result.success(dockerSandbox.execute(req.getCode(), LanguageCmdEnum.resolve(req.getLanguage())));
    }

}
