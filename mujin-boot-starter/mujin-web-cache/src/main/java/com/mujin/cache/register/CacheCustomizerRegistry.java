package com.mujin.cache.register;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.mujin.cache.annotations.EnableCacheCustomizer;
import com.mujin.cache.enums.CacheManagerEnum;
import com.mujin.cache.scan.RedisCacheNamePropertiesScanner;
import com.mujin.cache.scan.SimpleCacheNamePropertiesScanner;
import lombok.NonNull;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.cache.CacheType;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;

import java.util.Map;
import java.util.Objects;

/**
 * 注册
 *
 * @author chenglin.wu
 * @date 2026-05-01
 */
public class CacheCustomizerRegistry implements ImportBeanDefinitionRegistrar, EnvironmentAware {

    private Environment environment;

    private String[] basePackages;

    @Override
    public void setEnvironment(@NonNull Environment environment) {
        this.environment = environment;
    }

    @Override
    public void registerBeanDefinitions(@NonNull AnnotationMetadata annotationMetadata, @NonNull BeanDefinitionRegistry registry) {
        if (ObjectUtil.isNull(annotationMetadata)) {
            return;
        }
        Map<String, Object> annotationAttributes = annotationMetadata.getAnnotationAttributes(EnableCacheCustomizer.class.getName());

        if (CollectionUtil.isEmpty(annotationAttributes)) {
            return;
        }

        CacheType cacheType = this.environment.getProperty("spring.cache.type", CacheType.class, CacheType.SIMPLE);

        boolean allowRuntimeCreation = (boolean) annotationAttributes.get("allowRuntimeCreation");
        CacheManagerEnum annotationCacheType = (CacheManagerEnum) annotationAttributes.get("cacheType");


        RedisCacheNamePropertiesScanner redisScanner = null;
        SimpleCacheNamePropertiesScanner simpleScanner = null;
        if (CacheType.REDIS.equals(cacheType)) {
            redisScanner = new RedisCacheNamePropertiesScanner(registry, allowRuntimeCreation);
        }
        if (CacheType.SIMPLE.equals(cacheType) || CacheManagerEnum.SIMPLE.equals(annotationCacheType) || CacheManagerEnum.MIX.equals(annotationCacheType)) {
            simpleScanner = new SimpleCacheNamePropertiesScanner(registry, allowRuntimeCreation);

        }
        // redis 扫描
        if (Objects.nonNull(redisScanner)) {
            redisScanner.scan(this.getBasePackages(annotationMetadata.getClassName(), annotationAttributes));
        }
        // simple 缓存管理器扫描
        if (Objects.nonNull(simpleScanner)) {
            simpleScanner.scan(this.getBasePackages(annotationMetadata.getClassName(), annotationAttributes));
        }
    }

    /**
     * 获取扫描的基础路径
     *
     * @param annotationAttributes 当前注解信息
     * @return String
     * @author chenglin.wu
     * @date 2026-05-01
     */
    private String[] getBasePackages(String annotationClassName, Map<String, Object> annotationAttributes) {
        if (ArrayUtil.isEmpty(this.basePackages)) {
            this.basePackages = (String[]) annotationAttributes.get("basePackages");
            if (ArrayUtil.isEmpty(this.basePackages) || StrUtil.isBlank(this.basePackages[0])) {
                String packageName = ClassUtils.getPackageName(annotationClassName);
                this.basePackages = new String[]{packageName};
            }
        }
        return this.basePackages;
    }
}
