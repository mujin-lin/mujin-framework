package com.mujin.cache.caching;

import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * 缓存空间配置信息
 *
 * @author chenglin.wu
 * @date 2026-05-01
 */
public interface RedisCacheManagerPrefixCaching extends CacheManagerCacheNameCaching {
    /**
     * 获取配置的缓存名前缀
     *
     * @return String
     * @author chenglin.wu
     * @date 2026-05-01
     */
    String cachePrefix();

    /**
     * 缓存的key序列化器
     *
     * @return RedisSerializer<E>
     * @author chenglin.wu
     * @date 2026-05-01
     */
    RedisSerializer<String> keySerializer();

    /**
     * 缓存的value序列化器
     *
     * @return RedisSerializer<E>
     * @author chenglin.wu
     * @date 2026-05-01
     */
    RedisSerializer<?> valueSerializer();

}
