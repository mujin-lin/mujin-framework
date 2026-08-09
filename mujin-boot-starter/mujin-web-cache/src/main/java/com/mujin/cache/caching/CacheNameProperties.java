package com.mujin.cache.caching;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;

/**
 * 缓存名配置的接口
 *
 * @author chenglin.wu
 * @date 2026-05-01
 */
@Data
@NoArgsConstructor
public class CacheNameProperties {

    /**
     * 当前缓存名
     */
    private String cacheName;
    /**
     * 当前缓存名中所有键的过期时间
     */
    private Duration expiry;

    public CacheNameProperties(String cacheName, Duration expiry) {
        this.expiry = expiry;
        this.cacheName = cacheName;
    }

    public CacheNameProperties(String cacheName) {
        this(cacheName, null);
    }
}
