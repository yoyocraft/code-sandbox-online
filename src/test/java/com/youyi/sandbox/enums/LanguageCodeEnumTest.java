package com.youyi.sandbox.enums;

import com.youyi.sandbox.BaseUnitTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yoyocraft
 * @date 2024/09/18
 */
public class LanguageCodeEnumTest extends BaseUnitTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(LanguageCodeEnumTest.class);

    @Test
    public void test_resolve() {
        LanguageCmdEnum languageCmdEnum = LanguageCmdEnum.resolve("java");
        LOGGER.info("languageCmdEnum: {}", languageCmdEnum);
    }
}
