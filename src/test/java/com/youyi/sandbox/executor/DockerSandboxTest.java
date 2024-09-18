package com.youyi.sandbox.executor;

import com.youyi.sandbox.BaseUnitTest;
import com.youyi.sandbox.FileUtil;
import com.youyi.sandbox.enums.LanguageCmdEnum;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

/**
 * @author yoyocraft
 * @date 2024/09/18
 */
public class DockerSandboxTest extends BaseUnitTest {

    @Test
    public void test_c() throws IOException {
        String code = FileUtil.readFile("codes/main.c");
        ExecutorMessage executorMessage = DockerSandbox.execute(code, LanguageCmdEnum.C);
        Assert.assertTrue(executorMessage.getSuccess());
    }

    @Test
    public void test_cpp() throws IOException {
        String code = FileUtil.readFile("codes/main.cpp");
        ExecutorMessage executorMessage = DockerSandbox.execute(code, LanguageCmdEnum.CPP);
        Assert.assertTrue(executorMessage.getSuccess());
    }

    @Test
    public void test_java() throws IOException {
        String code = FileUtil.readFile("codes/Main.java");
        ExecutorMessage executorMessage = DockerSandbox.execute(code, LanguageCmdEnum.JAVA);
        Assert.assertTrue(executorMessage.getSuccess());
    }

    @Test
    public void test_python() throws IOException {
        String code = FileUtil.readFile("codes/main.py");
        ExecutorMessage executorMessage = DockerSandbox.execute(code, LanguageCmdEnum.PYTHON3);
        Assert.assertTrue(executorMessage.getSuccess());
    }

    @Test
    public void test_javascript() throws IOException {
        String code = FileUtil.readFile("codes/main.js");
        ExecutorMessage executorMessage = DockerSandbox.execute(code, LanguageCmdEnum.JAVASCRIPT);
        Assert.assertTrue(executorMessage.getSuccess());
    }

    @Test
    public void test_typescript() throws IOException {
        String code = FileUtil.readFile("codes/main.ts");
        ExecutorMessage executorMessage = DockerSandbox.execute(code, LanguageCmdEnum.TYPESCRIPT);
        Assert.assertTrue(executorMessage.getSuccess());
    }

}
