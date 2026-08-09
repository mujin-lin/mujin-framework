package com.mujin.cache.manager;


import cn.hutool.core.util.StrUtil;
import lombok.NonNull;
import org.springframework.cache.Cache;
import org.springframework.cache.support.AbstractCacheManager;

import java.util.Collection;
import java.util.Set;

/**
 * 本地缓存的cacheManager
 *
 * @author chenglin.wu
 * @date 2026-05-01
 */
public class SimpleLocalCacheManager extends AbstractCacheManager {


    private final Set<SimpleLocalExpireCache> caches;

    private final boolean allowRuntimeCacheCreation;


    public SimpleLocalCacheManager(Set<SimpleLocalExpireCache> caches, boolean allowRuntimeCacheCreation) {
        this.caches = caches;
        this.allowRuntimeCacheCreation = allowRuntimeCacheCreation;
    }

    @Override
    @NonNull
    protected Collection<? extends Cache> loadCaches() {
        return this.caches;
    }

    @Override
    protected Cache getMissingCache(@NonNull String name) {
        if (StrUtil.isBlank(name)) {
            throw new IllegalArgumentException("Cache name must not be null or empty");
        }
        return this.allowRuntimeCacheCreation ? this.runtimeCreateCache(name):super.getMissingCache(name);
    }

    /**
     * 运行时创建缓存
     *
     * @param name 缓存名
     * @return Cache
     * @author chenglin.wu
     * @date 2026-05-01
     */
    private Cache runtimeCreateCache(String name) {
        return new SimpleLocalExpireCache(name);
    }
}
