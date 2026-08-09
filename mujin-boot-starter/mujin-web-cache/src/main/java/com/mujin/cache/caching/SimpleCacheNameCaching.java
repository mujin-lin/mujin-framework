package com.mujin.cache.caching;

import org.springframework.core.serializer.Deserializer;
import org.springframework.core.serializer.Serializer;

/**
 * 简答的本地缓存缓存名和对应信息的配置
 *
 * @author chenglin.wu
 * @date 2026-05-01
 */
public interface SimpleCacheNameCaching extends CacheManagerCacheNameCaching {
    /**
     * 缓存值的序列化器
     *
     * @return Serializer<Object>
     * @author chenglin.wu
     * @date 2026-05-01
     */
    Serializer<Object> valueSerializer();

    /**
     * 缓存值的反序列化器
     *
     * @return Deserializer<Object>
     * @author chenglin.wu
     * @date 2026-05-01
     */
    Deserializer<Object> valueDeserializer();
}
