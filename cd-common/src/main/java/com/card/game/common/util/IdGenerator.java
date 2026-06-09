package com.card.game.common.util;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 简易ID生成器（基于时间戳 + 序列号）
 * 生产环境建议使用 Snowflake 算法
 */
public final class IdGenerator {

    private static final AtomicLong SEQUENCE = new AtomicLong(0);
    private static long lastTimestamp = -1;

    private IdGenerator() {}

    /**
     * 生成全局唯一ID
     * 格式：时间戳(41位) + 序列号(12位)
     */
    public static synchronized long nextId() {
        long timestamp = System.currentTimeMillis();

        if (timestamp == lastTimestamp) {
            long sequence = SEQUENCE.incrementAndGet() & 0xFFF; // 12位序列号
            if (sequence == 0) {
                // 序列号用尽，等待下一毫秒
                while (timestamp <= lastTimestamp) {
                    timestamp = System.currentTimeMillis();
                }
            }
        } else {
            SEQUENCE.set(0);
        }

        lastTimestamp = timestamp;

        // 时间戳左移12位 + 序列号
        return (timestamp << 12) | SEQUENCE.get();
    }

    /**
     * 获取UUID
     */
    public static String getUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 获取UUID(有前缀)
     */
    public static String getUUID(String prefix) {
        return prefix + getUUID();
    }

}
