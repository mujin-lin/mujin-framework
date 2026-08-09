package com.mujin.cache;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizer;
import org.springframework.boot.autoconfigure.cache.CacheManagerCustomizers;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

/**
 * redis 的缓存管理器自动配置类<br/>
 * 如果后面跨包引用，可以将此类加入到 spring.factories 文件中
 *
 * @author chenglin.wu
 * @date 2026-05-01
 */
public class CacheManagerAutoConfiguration {

    @Value("${spring.cache.cache-names:}")
    private List<String> cacheNames;

    @Value("${spring.cache.redis.enable-statistics:false}")
    private boolean enableStatistics;

    @Value("${spring.cache.redis.time-to-live:}")
    private Duration timeToLive;

    @Value("${spring.cache.redis.key-prefix:}")
    private String keyPrefix;

    @Value("${spring.cache.redis.cache-null-values:true}")
    private boolean cacheNullValues;

    @Value("${spring.cache.redis.use-key-prefix:true}")
    private boolean useKeyPrefix;

    @Value("${spring.application.name:}")
    private String applicationName;

    /**
     * 缓存管理器的用户其他配置
     *
     * @param customizers 用户其他配置信息
     * @return CacheManagerCustomizers
     * @author chenglin.wu
     * @date 2026-05-01
     */
    public CacheManagerCustomizers cacheManagerCustomizers(ObjectProvider<CacheManagerCustomizer<?>> customizers) {
        return new CacheManagerCustomizers(customizers.orderedStream().toList());
    }

    /**
     * redis cache manager 的注入
     *
     * @param customizers                         用户针对cacheManager 的配置
     * @param redisCacheConfiguration             redis默认的配置信息
     * @param redisCacheManagerBuilderCustomizers 用户自定义 cacheName的配置信息
     * @param redisConnectionFactory              redis连接工厂
     * @return RedisCacheManager
     * @author chenglin.wu
     * @date 2026-05-01
     */
    @Primary
    @Bean("cacheManager")
    @ConditionalOnProperty(value = "spring.cache.type", havingValue = "REDIS")
    @ConditionalOnBean(ReactiveRedisConnectionFactory.class)
    public RedisCacheManager redisCacheManager(CacheManagerCustomizers customizers,
                                               ObjectProvider<RedisCacheConfiguration> redisCacheConfiguration,
                                               ObjectProvider<RedisCacheManagerBuilderCustomizer> redisCacheManagerBuilderCustomizers,
                                               RedisConnectionFactory redisConnectionFactory) {
        RedisCacheManager.RedisCacheManagerBuilder builder = RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(
                        determineConfiguration(redisCacheConfiguration));
        if (CollectionUtil.isNotEmpty(this.cacheNames)) {
            builder.initialCacheNames(new LinkedHashSet<>(cacheNames));
        }
        if (this.enableStatistics) {
            builder.enableStatistics();
        }
        redisCacheManagerBuilderCustomizers.orderedStream().forEach((customizer) -> customizer.customize(builder));
        return customizers.customize(builder.build());
    }


    /**
     * 获取配置信息
     *
     * @param redisCacheConfiguration 缓存名和当前缓存名对应的配置信息
     * @return RedisCacheConfiguration
     * @author chenglin.wu
     * @date 2026-05-01
     */
    private RedisCacheConfiguration determineConfiguration(
            ObjectProvider<RedisCacheConfiguration> redisCacheConfiguration) {
        return redisCacheConfiguration.getIfAvailable(this::createConfiguration);
    }

    /**
     * redisCacheManager 创建
     *
     * @return RedisCacheConfiguration
     * @author chenglin.wu
     * @date 2026-05-01
     */
    private RedisCacheConfiguration createConfiguration() {
        RedisCacheConfiguration config = RedisCacheConfiguration
                .defaultCacheConfig();
        config = config.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.json()));
        // 设置默认过期时间
        Duration defaultTtl = Objects.isNull(this.timeToLive) ? Duration.ofMinutes(3L) : this.timeToLive;
        config = config.entryTtl(defaultTtl);
        // 设置默认缓存前缀
        String applicationName = StrUtil.isBlank(this.applicationName) ? StrUtil.EMPTY : this.applicationName + ":";
        String cachePrefix = StrUtil.blankToDefault(this.keyPrefix, "cache:" + applicationName);
        config = config.computePrefixWith(name -> cachePrefix + name + ":");
        if (!this.cacheNullValues) {
            config = config.disableCachingNullValues();
        }
        if (!this.useKeyPrefix) {
            config = config.disableKeyPrefix();
        }
        return config;
    }

}
