package com.youyi.sandbox.executor;

import com.youyi.sandbox.BaseUnitTest;
import com.youyi.sandbox.FileUtil;
import com.youyi.sandbox.enums.LanguageCmdEnum;
import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * @author yoyocraft
 * @date 2024/09/18
 */
public class DockerSandboxTest extends BaseUnitTest {

    @Mock
    DockerSandbox dockerSandbox;

    @Before
    @SneakyThrows
    public void setUp() {
        ExecutorMessage executorMessage = ExecutorMessage.builder().success(Boolean.TRUE).build();
        when(dockerSandbox.execute(anyString(), any(LanguageCmdEnum.class)))
                .thenReturn(executorMessage);
    }

    @Test
    public void test_c() throws IOException {
        String code = FileUtil.readFile("codes/main.c");
        ExecutorMessage executorMessage = dockerSandbox.execute(code, LanguageCmdEnum.C);
        Assert.assertTrue(executorMessage.getSuccess());
    }

    @Test
    public void test_cpp() throws IOException {
        String code = FileUtil.readFile("codes/main.cpp");
        ExecutorMessage executorMessage = dockerSandbox.execute(code, LanguageCmdEnum.CPP);
        Assert.assertTrue(executorMessage.getSuccess());
    }

    @Test
    public void test_java() throws IOException {
        String code = FileUtil.readFile("codes/Main.java");
        ExecutorMessage executorMessage = dockerSandbox.execute(code, LanguageCmdEnum.JAVA);
        Assert.assertTrue(executorMessage.getSuccess());
    }

    @Test
    public void test_python() throws IOException {
        String code = FileUtil.readFile("codes/main.py");
        ExecutorMessage executorMessage = dockerSandbox.execute(code, LanguageCmdEnum.PYTHON3);
        Assert.assertTrue(executorMessage.getSuccess());
    }

    @Test
    public void test_javascript() throws IOException {
        String code = FileUtil.readFile("codes/main.js");
        ExecutorMessage executorMessage = dockerSandbox.execute(code, LanguageCmdEnum.JAVASCRIPT);
        Assert.assertTrue(executorMessage.getSuccess());
    }

    @Test
    public void test_typescript() throws IOException {
        String code = FileUtil.readFile("codes/main.ts");
        ExecutorMessage executorMessage = dockerSandbox.execute(code, LanguageCmdEnum.TYPESCRIPT);
        Assert.assertTrue(executorMessage.getSuccess());
    }

}
