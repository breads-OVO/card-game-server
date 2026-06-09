package com.card.game.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * 时间工具类
 */
public class DateUtils {

    private static final String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String TIME_PATTERN = "HH:mm:ss";

    /**
     * 获取当前时间
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    /**
     * 格式化时间为字符串（默认格式：yyyy-MM-dd HH:mm:ss）
     */
    public static String format(LocalDateTime dateTime) {
        return format(dateTime, DEFAULT_PATTERN);
    }

    /**
     * 格式化时间为字符串（指定格式）
     */
    public static String format(LocalDateTime dateTime, String pattern) {
        if (dateTime == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return dateTime.format(formatter);
    }

    /**
     * 解析字符串为时间（默认格式：yyyy-MM-dd HH:mm:ss）
     */
    public static LocalDateTime parse(String dateTimeStr) {
        return parse(dateTimeStr, DEFAULT_PATTERN);
    }

    /**
     * 解析字符串为时间（指定格式）
     */
    public static LocalDateTime parse(String dateTimeStr, String pattern) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return LocalDateTime.parse(dateTimeStr, formatter);
    }

    /**
     * 计算两个时间之间的天数差
     */
    public static long daysBetween(LocalDateTime start, LocalDateTime end) {
        return ChronoUnit.DAYS.between(start, end);
    }

    /**
     * 计算两个时间之间的小时差
     */
    public static long hoursBetween(LocalDateTime start, LocalDateTime end) {
        return ChronoUnit.HOURS.between(start, end);
    }

    /**
     * 计算两个时间之间的分钟差
     */
    public static long minutesBetween(LocalDateTime start, LocalDateTime end) {
        return ChronoUnit.MINUTES.between(start, end);
    }

    /**
     * 计算两个时间之间的秒数差
     */
    public static long secondsBetween(LocalDateTime start, LocalDateTime end) {
        return ChronoUnit.SECONDS.between(start, end);
    }

    /**
     * 增加天数
     */
    public static LocalDateTime plusDays(LocalDateTime dateTime, long days) {
        return dateTime.plusDays(days);
    }

    /**
     * 增加小时
     */
    public static LocalDateTime plusHours(LocalDateTime dateTime, long hours) {
        return dateTime.plusHours(hours);
    }

    /**
     * 增加分钟
     */
    public static LocalDateTime plusMinutes(LocalDateTime dateTime, long minutes) {
        return dateTime.plusMinutes(minutes);
    }

    /**
     * 增加秒数
     */
    public static LocalDateTime plusSeconds(LocalDateTime dateTime, long seconds) {
        return dateTime.plusSeconds(seconds);
    }

    /**
     * 减少天数
     */
    public static LocalDateTime minusDays(LocalDateTime dateTime, long days) {
        return dateTime.minusDays(days);
    }

    /**
     * 减少小时
     */
    public static LocalDateTime minusHours(LocalDateTime dateTime, long hours) {
        return dateTime.minusHours(hours);
    }

    /**
     * 减少分钟
     */
    public static LocalDateTime minusMinutes(LocalDateTime dateTime, long minutes) {
        return dateTime.minusMinutes(minutes);
    }

    /**
     * 减少秒数
     */
    public static LocalDateTime minusSeconds(LocalDateTime dateTime, long seconds) {
        return dateTime.minusSeconds(seconds);
    }

    /**
     * 判断是否在指定时间之后
     */
    public static boolean isAfter(LocalDateTime dateTime, LocalDateTime other) {
        return dateTime.isAfter(other);
    }

    /**
     * 判断是否在指定时间之前
     */
    public static boolean isBefore(LocalDateTime dateTime, LocalDateTime other) {
        return dateTime.isBefore(other);
    }

    /**
     * 判断是否相等
     */
    public static boolean isEqual(LocalDateTime dateTime, LocalDateTime other) {
        return dateTime.isEqual(other);
    }

    /**
     * 获取当天的开始时间（00:00:00）
     */
    public static LocalDateTime getStartOfDay(LocalDateTime dateTime) {
        return dateTime.toLocalDate().atStartOfDay();
    }

    /**
     * 获取当天的结束时间（23:59:59.999999999）
     */
    public static LocalDateTime getEndOfDay(LocalDateTime dateTime) {
        return dateTime.toLocalDate().atTime(23, 59, 59, 999999999);
    }

    /**
     * 将毫秒时间戳转换为 LocalDateTime
     */
    public static LocalDateTime fromTimestamp(long timestamp) {
        return LocalDateTime.ofEpochSecond(timestamp / 1000,
                (int) (timestamp % 1000 * 1000000),
                java.time.ZoneOffset.ofHours(8));
    }

    /**
     * 将 LocalDateTime 转换为毫秒时间戳
     */
    public static long toTimestamp(LocalDateTime dateTime) {
        return dateTime.atZone(java.time.ZoneOffset.ofHours(8)).toInstant().toEpochMilli();
    }
}

