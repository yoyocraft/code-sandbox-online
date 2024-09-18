package com.youyi.sandbox;

import com.youyi.sandbox.constants.SymbolConstant;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;

/**
 * @author yoyocraft
 * @date 2024/09/18
 */
public class FileUtil {
    public static String readFile(String fileName) {
        ClassLoader classLoader = FileUtil.class.getClassLoader();
        URL url = classLoader.getResource(fileName);
        assert url != null;

        try (BufferedReader reader = new BufferedReader(new FileReader(url.getFile()))) {
            String line;
            StringBuilder tcsBuilder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                // 跳过空行
                if (line.trim().isEmpty()) {
                    continue;
                }
                tcsBuilder.append(line).append(SymbolConstant.NEW_LINE);
            }
            return tcsBuilder.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
