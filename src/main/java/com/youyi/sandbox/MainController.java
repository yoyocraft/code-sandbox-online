package com.youyi.sandbox;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 系统检查使用，请勿删除
 * @author yoyocraft
 * @date 2024/09/17
 */
@Controller
public class MainController {

    @GetMapping("/check")
    public String check() {
        return "success";
    }


}
