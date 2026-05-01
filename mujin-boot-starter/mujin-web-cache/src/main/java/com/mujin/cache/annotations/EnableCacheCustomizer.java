package com.mujin.cache.annotations;

import com.mujin.cache.enums.CacheManagerEnum;
import com.mujin.cache.register.CacheCustomizerRegistry;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * 开启扫描缓存 name 和缓存过期时间的配置信息
 * <br/>
 * 使用当前注解在启动类里面则不需要再使用@EnableCaching 开启缓存
 *
 * @author chenglin.wu
 * @date 2026-05-01
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@EnableCaching
@Import(CacheCustomizerRegistry.class)
public @interface EnableCacheCustomizer {
    /**
     * 基础包扫描路径
     *
     * @return String
     * @author chenglin.wu
     * @date 2026-05-01
     */
    @AliasFor("basePackages")
    String[] value() default "";

    /**
     * 基础包扫描路径
     *
     * @return String
     * @author chenglin.wu
     * @date 2026-05-01
     */
    @AliasFor("value")
    String[] basePackages() default "";

    /**
     * 当未找到对应的cache name时候是否允许在运行时创建对应的cache
     *
     * @return boolean
     * @author chenglin.wu
     * @date 2026-05-01
     */
    boolean allowRuntimeCreation() default true;

    /**
     * 当前缓存管理器类型，当前配置与 spring.cache.type 不一致时，以 spring.cache.type 配置的为准
     *
     * @return CacheManagerEnum
     * @author chenglin.wu
     * @date 2026-05-01
     */
    CacheManagerEnum cacheType() default CacheManagerEnum.SIMPLE;
}
