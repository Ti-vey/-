package com.warehouse;

import cn.hutool.crypto.digest.BCrypt;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 密码加密工具单元测试。
 *
 * 系统约定用户密码使用 BCrypt 算法加密后存储，本测试用于验证：
 * 1. 同一明文经加密后可以用 checkpw 校验通过；
 * 2. 错误密码无法通过校验；
 * 3. 由于 BCrypt 自带随机盐，同一明文两次加密得到的密文不相同。
 */
public class PasswordGeneratorTest {

    private static final String RAW_PASSWORD = "123456";

    @Test
    public void shouldMatchRawPassword() {
        String hash = BCrypt.hashpw(RAW_PASSWORD, BCrypt.gensalt());
        assertTrue(BCrypt.checkpw(RAW_PASSWORD, hash), "正确密码应校验通过");
        assertFalse(BCrypt.checkpw("wrong-password", hash), "错误密码不应通过校验");
    }

    @Test
    public void shouldGenerateDifferentHashEachTime() {
        String first = BCrypt.hashpw(RAW_PASSWORD, BCrypt.gensalt());
        String second = BCrypt.hashpw(RAW_PASSWORD, BCrypt.gensalt());
        assertFalse(first.equals(second), "相同明文的两次加密结果应因随机盐而不同");
        assertTrue(BCrypt.checkpw(RAW_PASSWORD, second));
    }
}
