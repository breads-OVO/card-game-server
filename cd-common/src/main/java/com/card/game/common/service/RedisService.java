package com.card.game.common.service;


import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis 服务接口
 * 定义常用的 Redis 操作
 */
public interface RedisService {

    // ==================== String 操作 ====================

    /**
     * 设置键值对
     */
    void set(String key, String value);

    /**
     * 设置键值对（带过期时间）
     */
    void set(String key, String value, long timeout, TimeUnit unit);

    /**
     * 设置键值对（带过期时间，秒）
     */
    void setEx(String key, String value, long seconds);

    /**
     * 获取值
     */
    String get(String key);

    /**
     * 删除键
     */
    Boolean delete(String key);

    /**
     * 批量删除
     */
    Long delete(Collection<String> keys);

    /**
     * 检查键是否存在
     */
    Boolean exists(String key);

    /**
     * 设置过期时间
     */
    Boolean expire(String key, long timeout, TimeUnit unit);

    /**
     * 获取剩余过期时间（秒）
     */
    Long getExpire(String key);

    /**
     * 递增
     */
    Long increment(String key, long delta);

    // ==================== Object 操作 ====================

    /**
     * 设置对象
     */
    void setObject(String key, Object value);

    /**
     * 设置对象（带过期时间）
     */
    void setObject(String key, Object value, long timeout, TimeUnit unit);

    /**
     * 获取对象
     */
    <T> T getObject(String key, Class<T> clazz);

    // ==================== Hash 操作 ====================

    /**
     * 设置 Hash 字段
     */
    void hSet(String key, String field, Object value);

    /**
     * 获取 Hash 字段
     */
    <T> T hGet(String key, String field, Class<T> clazz);


    /**
     * 删除 Hash 字段
     */
    Long hDelete(String key, String... fields);

    /**
     * 判断 Hash 字段是否存在
     */
    Boolean hExists(String key, String field);

    // ==================== Set 操作 ====================

    /**
     * 添加 Set 元素
     */
    Long sAdd(String key, String... values);

    /**
     * 获取 Set 所有元素
     */
    Set<String> sMembers(String key);

    /**
     * 判断是否是 Set 元素
     */
    Boolean sIsMember(String key, String value);

    /**
     * 删除 Set 元素
     */
    Long sRemove(String key, String... values);

    // ==================== 分布式锁 ====================

    /**
     * 获取分布式锁
     * @param key 锁的键
     * @param value 锁的值（用于释放时验证）
     * @param timeout 超时时间（秒）
     * @return 是否获取成功
     */
    boolean tryLock(String key, String value, long timeout);

    /**
     * 释放分布式锁
     */
    void unlock(String key, String value);
}
