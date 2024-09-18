package com.youyi.sandbox.enums;

import com.youyi.sandbox.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * @author yoyocraft
 * @date 2024/09/17
 */
@Getter
@AllArgsConstructor
public enum LanguageCmdEnum {

    // TODO youyi 2024/9/18 提取出常量
    JAVA("java", "Main.java", new String[]{"javac", "-encoding", "UTF-8", "Main.java"}, new String[]{"java", "-Dfile.encoding=UTF-8", "Main"}),
    CPP("cpp", "main.cpp", new String[]{"g++", "-finput-charset=UTF-8", "-fexec-charset=UTF-8", "-o", "main", "main.cpp"}, new String[]{"./main"}),
    C("c", "main.c", new String[]{"gcc", "-finput-charset=UTF-8", "-fexec-charset=UTF-8", "-o", "main", "main.c"}, new String[]{"./main"}),
    PYTHON3("python", "main.py", null, new String[]{"python3", "main.py"}),
    JAVASCRIPT("javascript", "main.js", null, new String[]{"node", "main.js"}),
    TYPESCRIPT("typescript", "main.ts", null, new String[]{"node", "main.ts"}),
    GO("go", "main.go", null, new String[]{"go", "run", "main.go"}),
    ;

    private final String language;
    private final String fileName;
    private final String[] compileCmd;
    private final String[] runCmd;

    public static LanguageCmdEnum resolve(String language) {
        if (StringUtils.isBlank(language)) {
            throw new BusinessException("language is blank");
        }

        try {
            return LanguageCmdEnum.valueOf(language.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("language is not support");
        }
    }
}
