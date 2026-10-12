package com.warehouse.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 单据编号生成工具。
 * 编号规则：单据前缀 + 年月日时分秒 + 两位随机数，兼顾可读性与唯一性。
 */
public final class OrderNoUtil {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private OrderNoUtil() {
    }

    public static String generate(String prefix) {
        return prefix + LocalDateTime.now().format(FORMATTER)
                + ThreadLocalRandom.current().nextInt(10, 100);
    }
}
