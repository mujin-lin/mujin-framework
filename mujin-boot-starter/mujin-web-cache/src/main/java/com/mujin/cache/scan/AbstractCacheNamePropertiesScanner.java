package com.mujin.cache.scan;

import cn.hutool.core.collection.CollectionUtil;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.SpringProperties;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author chenglin.wu
 * @date 2026-05-01
 */
@Slf4j
public abstract class AbstractCacheNamePropertiesScanner extends ClassPathBeanDefinitionScanner {
    /**
     * resource 路径读取解析器
     */
    private final ResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver();
    /**
     * 是否要忽略类格式化异常
     */
    private final boolean shouldIgnoreClassFormatException = SpringProperties.getFlag(IGNORE_CLASSFORMAT_PROPERTY_NAME);
    /**
     * 当前类的类加载器
     */
    private final ClassLoader classLoader = this.getClass().getClassLoader();

    /**
     * 是否允许在运行中创建 cache
     */
    private boolean allowRuntimeCreation = true;

    public AbstractCacheNamePropertiesScanner(BeanDefinitionRegistry registry, boolean allowRuntimeCreation) {
        super(registry);
        this.allowRuntimeCreation = allowRuntimeCreation;

        Set<TypeFilter> initIncludeFilters = this.initIncludeFilter();
        if (CollectionUtil.isNotEmpty(initIncludeFilters)) {
            initIncludeFilters.stream().filter(Objects::nonNull).forEach(super::addIncludeFilter);
        }
        Set<TypeFilter> excludeFilters = this.initExcludeFilter();

        if (CollectionUtil.isNotEmpty(excludeFilters)) {
            excludeFilters.stream().filter(Objects::nonNull).forEach(super::addExcludeFilter);
        }
        // bean name 生成器
        super.setBeanNameGenerator(new RepeatBeanNameGenerator(AnnotationBeanNameGenerator.INSTANCE));

    }

    /**
     * 构造出对应 bean definition
     *
     * @param basePackage 扫描包
     * @return Set<BeanDefinition>
     * @author chenglin.wu
     * @date 2026-05-01
     */
    @Override
    @NonNull
    public Set<BeanDefinition> findCandidateComponents(@NonNull String basePackage) {
        Resource[] resources = this.scanResources(basePackage);
        return this.doScanCandidateComponents(resources);
    }

    /**
     * 能够扫描构造 Bean 的 filter
     *
     * @return Set<TypeFilter>
     * @author chenglin.wu
     * @date 2026-05-01
     */
    protected abstract Set<TypeFilter> initIncludeFilter();

    /**
     * 不能够扫描构造 Bean 的 filter
     *
     * @return Set<TypeFilter>
     * @author chenglin.wu
     * @date 2026-05-01
     */
    protected abstract Set<TypeFilter> initExcludeFilter();

    /**
     * 根据当前类资源路径创建对应的 bean definition
     *
     * @param resources 扫描到的类资源路径
     * @return Set<BeanDefinition>
     * @author chenglin.wu
     * @date 2026-05-01
     */
    protected abstract Set<BeanDefinition> doScanCandidateComponents(Resource[] resources);

    /**
     * 获取类资源解析器
     *
     * @return ResourcePatternResolver
     * @author chenglin.wu
     * @date 2026-05-01
     */
    protected ResourcePatternResolver getPatternResolver() {
        return patternResolver;
    }

    /**
     * 是否允许在未找到缓存名时创建默认的缓存
     *
     * @return boolean
     * @author chenglin.wu
     * @date 2026-05-01
     */
    protected boolean isAllowRuntimeCreation() {
        return allowRuntimeCreation;
    }

    /**
     * 是否忽略类格式化异常
     *
     * @return boolean
     * @author chenglin.wu
     * @date 2026-05-01
     */
    protected boolean isShouldIgnoreClassFormatException() {
        return shouldIgnoreClassFormatException;
    }

    /**
     * 获取类加载器
     *
     * @return ClassLoader
     * @author chenglin.wu
     * @date 2026-05-01
     */
    protected ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * 根据基础包名扫描对应的 class 文件
     *
     * @param basePackage 基础包信息
     * @return Resource 扫描到的class文件信息
     * @author chenglin.wu
     * @date 2026-05-01
     */
    protected Resource[] doScanResources(String basePackage) throws IOException {
        String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX +
                resolveBasePackage(basePackage) + "/**/*.class";
        return this.patternResolver.getResources(packageSearchPath);
    }

    /**
     * 使用自定义的调用方法，包裹报错信息
     *
     * @param basePackage 扫描的包信息
     * @return Resource
     * @author chenglin.wu
     * @date 2026-05-01
     */
    private Resource[] scanResources(String basePackage) {
        try {
            return this.doScanResources(basePackage);
        } catch (IOException ex) {
            throw new BeanDefinitionStoreException("I/O failure during classpath scanning", ex);
        }
    }

    /**
     * 自定的class type filter
     */
    @EqualsAndHashCode
    protected class CachingTypeFilter implements TypeFilter {
        /**
         * 是否为include筛选 true是include的筛选，false的exclude的筛选
         */
        private final boolean includeFlag;
        /**
         * 父级接口或者父类的 class 对象
         */
        private final Class<?> superClass;


        public CachingTypeFilter(Class<?> superClass) {
            this(superClass, true);
        }

        public CachingTypeFilter(Class<?> superClass, boolean includeFlag) {
            this.includeFlag = includeFlag;
            this.superClass = superClass;
        }

        @Override
        public boolean match(MetadataReader metadataReader, @NonNull MetadataReaderFactory metadataReaderFactory) {
            return includeFlag == include(metadataReader.getClassMetadata());
        }

        /**
         * 对当前类的include检查，看是否要将当前类注入IOC
         *
         * @param classMetadata class 数据读取
         * @return boolean
         * @author chenglin.wu
         * @date 2026-05-01
         */
        private boolean include(ClassMetadata classMetadata) {
            // 如果当前扫描到的类是接口或者是抽象类，则返回false 不包含
            if (classMetadata.isInterface() || classMetadata.isAbstract()) {
                return false;
            }
            Class<?> aClass = null;
            try {
                // 获取当前类的class对象
                aClass = Class.forName(classMetadata.getClassName(), false, AbstractCacheNamePropertiesScanner.this.classLoader);
            } catch (ClassNotFoundException e) {
                log.error("未找到缓存扫描类: ", e);
            }
            if (Objects.isNull(aClass)) {
                return false;
            }

            // 针对class字节码对象的验证
            return includeClassCheck(aClass);
        }

        /**
         * 对当前类的 class 对象的检查
         *
         * @param clazz 当前扫描到的类的 class 对象
         * @return boolean
         * @author chenglin.wu
         * @date 2026-05-01
         */
        private boolean includeClassCheck(Class<?> clazz) {
            // 获取无参构造，如果当前class对象找不到无参构造，或者无参构造方法是受保护的或私有的，则排除当前扫描到的类
            Constructor<?> constructor = null;
            try {
                constructor = clazz.getConstructor();
            } catch (NoSuchMethodException e) {
                log.error("当前对象没有无参构造方法，构建对象失败：", e);
            }
            if (Objects.isNull(constructor)
                    || Modifier.isProtected(constructor.getModifiers())
                    || Modifier.isPrivate(constructor.getModifiers())) {
                return false;
            }
            return this.superClass.isAssignableFrom(clazz);
        }
    }

    /**
     * 需要重复注入同样类的 bean 名生成器
     */
    protected static class RepeatBeanNameGenerator implements BeanNameGenerator {

        private final BeanNameGenerator beanNameGenerator;

        private final Map<String, AtomicInteger> beanNameCounterMap = new ConcurrentHashMap<>(16);

        public RepeatBeanNameGenerator(BeanNameGenerator beanNameGenerator) {
            this.beanNameGenerator = beanNameGenerator;
        }

        @Override
        @NonNull
        public String generateBeanName(@NonNull BeanDefinition definition, @NonNull BeanDefinitionRegistry registry) {
            String beanName = this.beanNameGenerator.generateBeanName(definition, registry);
            AtomicInteger atomicInteger = this.beanNameCounterMap.get(beanName);
            if (Objects.nonNull(atomicInteger)) {
                return beanName + atomicInteger.incrementAndGet();
            }
            atomicInteger = new AtomicInteger(0);
            this.beanNameCounterMap.put(beanName, atomicInteger);
            return beanName;
        }
    }
}
