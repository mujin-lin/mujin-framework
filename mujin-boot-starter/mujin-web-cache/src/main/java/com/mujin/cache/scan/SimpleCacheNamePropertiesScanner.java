package com.mujin.cache.scan;

import com.mujin.cache.caching.SimpleCacheNameCaching;
import com.mujin.cache.enums.CacheManagerEnum;
import com.mujin.cache.manager.SimpleLocalCacheManager;
import com.mujin.cache.manager.SimpleLocalExpireCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.core.io.Resource;
import org.springframework.core.serializer.Deserializer;
import org.springframework.core.serializer.Serializer;
import org.springframework.core.serializer.support.SerializationDelegate;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 简单缓存的扫描器<br/>
 * 扫描 {@link SimpleCacheNameCaching 的实现类}
 *
 * @author chenglin.wu
 * @date 2026-05-01
 */
@Slf4j
public class SimpleCacheNamePropertiesScanner extends AbstractCacheNamePropertiesScanner {


    public SimpleCacheNamePropertiesScanner(BeanDefinitionRegistry registry, boolean allowRuntimeCreation) {
        super(registry, allowRuntimeCreation);
        super.setBeanNameGenerator((definition, registry1) -> CacheManagerEnum.SIMPLE.getManagerName());
    }

    @Override
    protected Set<TypeFilter> initIncludeFilter() {
        return Set.of(new CachingTypeFilter(SimpleCacheNameCaching.class));
    }

    @Override
    protected Set<TypeFilter> initExcludeFilter() {
        return Set.of(new CachingTypeFilter(SimpleCacheNameCaching.class, false));
    }

    @Override
    protected Set<BeanDefinition> doScanCandidateComponents(Resource[] resources) {
        Set<SimpleLocalExpireCache> expireCaches = new HashSet<>();
        // 获取 SimpleCacheManager中对Cache的配置
        for (Resource resource : resources) {
            String filename = resource.getFilename();
            if (filename != null && filename.contains(ClassUtils.CGLIB_CLASS_SEPARATOR)) {
                // Ignore CGLIB-generated classes in the classpath
                continue;
            }
            // 执行创建 cache的方法
            try {
                this.createSimpleLocalExpireCaches(resource, expireCaches);
            } catch (IOException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
                     InstantiationException | IllegalAccessException e) {
                log.warn("create simple cache bean definition failure!");
            }
        }
        // 构造SimpleLocalCacheManager 对象的 beanDefinition
        GenericBeanDefinition beanDefinition = (GenericBeanDefinition) BeanDefinitionBuilder.genericBeanDefinition(SimpleLocalCacheManager.class)
                .addConstructorArgValue(expireCaches)
                .addConstructorArgValue(super.isAllowRuntimeCreation())
                .setScope(BeanDefinition.SCOPE_SINGLETON)
                .setAutowireMode(GenericBeanDefinition.AUTOWIRE_BY_TYPE)
                .getBeanDefinition();
        return Set.of(beanDefinition);
    }

    /**
     * 创建SimpleLocalExpireCache对象
     *
     * @param resource     扫描到的类class文件信息
     * @param expireCaches 所有的缓存cache的配置
     * @author chenglin.wu
     * @date 2026-05-01
     */
    private void createSimpleLocalExpireCaches(Resource resource, Set<SimpleLocalExpireCache> expireCaches) throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        MetadataReader metadataReader = super.getMetadataReaderFactory().getMetadataReader(resource);
        if (!super.isCandidateComponent(metadataReader)) {
            return;
        }

        Class<?> cachingClass = ClassUtils.forName(metadataReader.getClassMetadata().getClassName(), super.getClassLoader());
        Constructor<?> constructor = cachingClass.getConstructor();

        SimpleCacheNameCaching caching = (SimpleCacheNameCaching) constructor.newInstance();

        Serializer<Object> objectSerializer = caching.valueSerializer();
        Deserializer<Object> objectDeserializer = caching.valueDeserializer();

        SerializationDelegate serialization = new SerializationDelegate(objectSerializer, objectDeserializer);

        Set<SimpleLocalExpireCache> collect = caching.cacheNames().stream().filter(Objects::nonNull)
                .map(item ->
                        new SimpleLocalExpireCache(item.getCacheName(), item.getExpiry(), true, serialization)
                )
                .collect(Collectors.toSet());

        expireCaches.addAll(collect);
    }
}
