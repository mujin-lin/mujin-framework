package com.mujin.cache.customizer;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.mujin.cache.caching.CacheNameProperties;
import com.mujin.cache.caching.RedisCacheManagerPrefixCaching;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 当前系统用户自定义缓存空间配置
 *
 * @author chenglin.wu
 * @date 2026-05-01
 */
public record CacheManagerBuilderCustomizer(RedisCacheManagerPrefixCaching redisCacheManagerPrefixCaching,
                                            boolean allowRuntimeCreation) implements RedisCacheManagerBuilderCustomizer {

    @Override
    public void customize(RedisCacheManager.RedisCacheManagerBuilder builder) {
        if (CollectionUtil.isEmpty(this.redisCacheManagerPrefixCaching.cacheNames())) {
            return;
        }

        RedisCacheConfiguration config = builder.cacheDefaults();
        // 设置键值的序列化器
        RedisSerializer<String> keySerializer = this.redisCacheManagerPrefixCaching.keySerializer();
        if (Objects.nonNull(keySerializer)) {
            config = config.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(keySerializer));
        }
        if (Objects.nonNull(this.redisCacheManagerPrefixCaching.valueSerializer())) {
            config = config.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(this.redisCacheManagerPrefixCaching.valueSerializer()));
        }
        // 设置缓存前缀
        String cachePrefix = StrUtil.blankToDefault(this.redisCacheManagerPrefixCaching.cachePrefix(), "cache:");
        RedisCacheConfiguration preFixConfig = config.computePrefixWith(name -> cachePrefix + name + ":");

        // 获取缓存的配置
        Map<String, RedisCacheConfiguration> cachePropertiesMap = this.redisCacheManagerPrefixCaching.cacheNames().stream()
                .collect(
                        Collectors.toMap(
                                CacheNameProperties::getCacheName,
                                // 如果传入的过期时间为空，则使用默认的过期时间配置
                                item -> Objects.isNull(item.getExpiry()) ? preFixConfig : preFixConfig.entryTtl(item.getExpiry()),
                                (oldValue, newValue) -> newValue
                        )
                );
        // builder 设置数据
        builder.initialCacheNames(cachePropertiesMap.keySet());
        builder.withInitialCacheConfigurations(cachePropertiesMap);
        builder.allowCreateOnMissingCache(this.allowRuntimeCreation);
    }
}
