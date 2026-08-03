package com.shop.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

public final class OrderNoGenerator {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private OrderNoGenerator() {
    }

    public static String generate(String prefix) {
        int random = ThreadLocalRandom.current().nextInt(100000, 999999);
        return prefix + LocalDateTime.now().format(FORMATTER) + random;
    }
}

