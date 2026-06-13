package com.card.game.match.constant;

public class RedisKeysConstants {

    /** Redis Sorted Set 队列 Key 前缀 */
    public static final String MATCH_QUEUE_KEY = "match:queue:";
    /**  匹配 分布式锁 Key 前缀 */
    public static final String MATCH_LOCK_KEY = "match:lock:";
    /**  匹配 玩家信息 Key 前缀 */
    public static final String MATCH_PLAYER_KEY = "match:player:";


    /**  段位分变更 Key 前缀 */
    public static final String RATING_DONE_KEY = "match:rating:done:";
}
