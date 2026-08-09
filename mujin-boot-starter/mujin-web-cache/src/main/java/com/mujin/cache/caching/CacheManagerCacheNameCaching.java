package com.mujin.cache.caching;


import java.util.List;

/**
 * 缓存管理器缓存名的配置
 *
 * @author chenglin.wu
 * @date 2026-05-01
 */
public interface CacheManagerCacheNameCaching {
    /**
     * 获取缓存空间的过期时间配置
     *
     * @return List<CacheNameProperties>
     * @author chenglin.wu
     * @date 2026-05-01
     */
    List<CacheNameProperties> cacheNames();

}
