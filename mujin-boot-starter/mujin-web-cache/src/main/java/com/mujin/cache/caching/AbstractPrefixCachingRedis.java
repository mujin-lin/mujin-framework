package com.mujin.cache.caching;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * 当前系统默认的 Redis 缓存前缀和键值对序列化器
 *
 * @author chenglin.wu
 * @date 2026-05-01
 */
public abstract class AbstractPrefixCachingRedis implements RedisCacheManagerPrefixCaching {
    @Override
    public String cachePrefix() {
        return "";
    }

    @Override
    public RedisSerializer<Object> valueSerializer() {
        return RedisSerializer.json();
    }

    @Override
    public RedisSerializer<String> keySerializer() {
        return StringRedisSerializer.UTF_8;
    }
}
