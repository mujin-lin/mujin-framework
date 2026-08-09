package com.mujin.cache.enums;

import lombok.Getter;
import org.springframework.cache.annotation.Cacheable;

/**
 * 缓存管理器的类型
 *
 * @author chenglin.wu
 * @date 2026-05-01
 */
@Getter
public enum CacheManagerEnum {
    /**
     * 简单的内存,进行缓存
     */
    SIMPLE("simpleCacheManager"),
    /**
     * redis 进行缓存
     */
    REDIS("cacheManager"),
    /**
     * redis和 simple都进行使用，由用户在 cacheable中的 cacheManager中指定<br/>
     * 使用此种类型是redis的名字为 cacheManager，simple为 simpleCacheManager
     * {@link Cacheable#cacheManager()}
     */
    MIX("mixCacheManager");

    /**
     * 缓存管理器的名字
     */
    private final String managerName;

    CacheManagerEnum(String managerName){
        this.managerName=managerName;
    }

}
