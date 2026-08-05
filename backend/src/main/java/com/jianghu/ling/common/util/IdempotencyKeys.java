package com.jianghu.ling.common.util;

import java.util.UUID;

public final class IdempotencyKeys {

    private IdempotencyKeys() {
    }

    public static String bizNo(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().replace("-", "");
    }
}
