package com.mujin.cache.scan;

import com.mujin.cache.caching.RedisCacheManagerPrefixCaching;
import com.mujin.cache.customizer.CacheManagerBuilderCustomizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 当前类路径的下扫描信息
 * <br/>
 * 返回实现了 {@link RedisCacheManagerPrefixCaching} 接口的类
 *
 * @author chenglin.wu
 * @date 2025-07-17
 */
@Slf4j
public class RedisCacheNamePropertiesScanner extends AbstractCacheNamePropertiesScanner {
    /**
     * cache manager 自定义配置的实现类class对象
     */
    private final Class<CacheManagerBuilderCustomizer> customizerClass = CacheManagerBuilderCustomizer.class;
    /**
     * cache manager 自定义配置的实现类的class资源路径
     */
    private final Resource customizerResource;


    public RedisCacheNamePropertiesScanner(BeanDefinitionRegistry registry, boolean allowRuntimeCreation) {
        super(registry, allowRuntimeCreation);
        this.customizerResource = super.getPatternResolver().getResource(ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + "/com/cloud/pedigree/common/cache/customizer/CacheManagerBuilderCustomizer.class");
    }

    @Override
    protected Set<TypeFilter> initIncludeFilter() {
        return Set.of(new CachingTypeFilter(RedisCacheManagerPrefixCaching.class));
    }

    @Override
    protected Set<TypeFilter> initExcludeFilter() {
        return Set.of(new CachingTypeFilter(RedisCacheManagerPrefixCaching.class, false));
    }


    @Override
    protected Set<BeanDefinition> doScanCandidateComponents(Resource[] resources) {
        Set<BeanDefinition> candidates = new LinkedHashSet<>();
        // 循环resources 封装 bean definition
        for (Resource resource : resources) {
            String filename = resource.getFilename();
            if (filename != null && filename.contains(ClassUtils.CGLIB_CLASS_SEPARATOR)) {
                // Ignore CGLIB-generated classes in the classpath
                continue;
            }
            try {
                // 调用实际生成 bean definition 的方法
                this.doCreateBeanDefinition(resource, candidates);
            } catch (IOException ex) {
                log.trace("Ignored non-readable {}: {}", resource, ex.getMessage());
            } catch (InvocationTargetException | InstantiationException | NoSuchMethodException |
                     ClassNotFoundException | IllegalAccessException ex) {
                if (super.isShouldIgnoreClassFormatException()) {
                    log.debug("Ignored incompatible class format in {}:{} ", resource, ex.getMessage());
                } else {
                    throw new BeanDefinitionStoreException("Incompatible class format in " + resource +
                            ": set system property 'spring.classformat.ignore' to 'true' " +
                            "if you mean to ignore such files during classpath scanning", ex);
                }
            }
        }
        return candidates;
    }

    /**
     * 实际生成 bean definition 的方法
     *
     * @param resource   当前 caching 实现类对应的resource
     * @param candidates 保存bean definition的集合
     * @author chenglin.wu
     * @date 2026-05-01
     */
    private void doCreateBeanDefinition(Resource resource, Set<BeanDefinition> candidates) throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        MetadataReader metadataReader = super.getMetadataReaderFactory().getMetadataReader(resource);
        if (super.isCandidateComponent(metadataReader)) {
            // 缓存的 caching的class 对象
            Class<?> aClass = Class.forName(metadataReader.getClassMetadata().getClassName(), false, super.getClassLoader());
            RedisCacheManagerPrefixCaching caching = (RedisCacheManagerPrefixCaching) aClass.getConstructor().newInstance();

            // 构造自定义配置的 cache name 和当前cache name 过期时间的 beanDefinition
            GenericBeanDefinition beanDefinition = (GenericBeanDefinition) BeanDefinitionBuilder.genericBeanDefinition(this.customizerClass)
                    .addConstructorArgValue(caching)
                    .addConstructorArgValue(super.isAllowRuntimeCreation())
                    .setAutowireMode(GenericBeanDefinition.AUTOWIRE_BY_TYPE)
                    .setScope(BeanDefinition.SCOPE_SINGLETON)
                    .getBeanDefinition();
            beanDefinition.setSource(this.customizerResource);

            candidates.add(beanDefinition);
        } else {
            log.trace("Ignored because not matching any filter: {}", resource);
        }
    }
}
