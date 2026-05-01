package com.mujin.cache.manager;

import lombok.EqualsAndHashCode;
import net.jodah.expiringmap.ExpiringMap;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.core.serializer.support.SerializationDelegate;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 本地可过期的缓存配置
 *
 * @author chenglin.wu
 * @date 2026-05-01
 */
@EqualsAndHashCode(callSuper = true)
public class SimpleLocalExpireCache extends ConcurrentMapCache {

    private static final Duration DEFAULT_EXPIRE = Duration.ofSeconds(900L);


    public SimpleLocalExpireCache(String name) {
        this(name, DEFAULT_EXPIRE);
    }

    public SimpleLocalExpireCache(String name, Duration expire) {
        this(name, expire, true);
    }

    public SimpleLocalExpireCache(String name, Duration expire, boolean allowNullValues) {
        this(name, expire, allowNullValues, null);
    }

    public SimpleLocalExpireCache(String name, Duration expire, boolean allowNullValues, SerializationDelegate serialization) {
        super(name, ExpiringMap.builder().expiration(expire.toSeconds(), TimeUnit.SECONDS).variableExpiration().build(), allowNullValues, serialization);
    }

}
