package com.mujin.orm;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.mujin.orm.annotations.EnableAutoFill;
import com.mujin.orm.handler.InsertFillColumnHandler;
import com.mujin.orm.handler.UpdateFillColumnHandler;
import com.mujin.orm.handler.impl.CreteTimeFillHandler;
import com.mujin.orm.handler.impl.DelFlagFillHandler;
import com.mujin.orm.handler.impl.UpdateTimeFillHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.lang.NonNull;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 自动注入类的register,用于查找接口<br/>
 * InsertFillColumnHandler 和 UpdateFillColumnHandler 对应的实现类
 *
 * @author chenglin.wu
 * @date 2025/12/27 22:22
 */
@Slf4j
public class AutoFillRegister implements ImportBeanDefinitionRegistrar, BeanFactoryAware, Ordered {

    // 框架默认填充处理器
    private static final Set<Class<?>> DEFAULT_FRAMEWORK_HANDLERS = new HashSet<>(Arrays.asList(
            CreteTimeFillHandler.class,
            UpdateTimeFillHandler.class,
            DelFlagFillHandler.class
    ));

    // 要扫描的核心接口
    private static final Class<?>[] TARGET_INTERFACES = {
            InsertFillColumnHandler.class,
            UpdateFillColumnHandler.class
    };


    // 安全获取的BeanFactory（通过BeanFactoryAware回调）
    private BeanFactory beanFactory;

    // ========== 安全获取BeanFactory（核心：无反射） ==========
    @Override
    public void setBeanFactory(@NonNull BeanFactory beanFactory) throws BeansException {
        // Spring生命周期回调，直接注入，无反射
        this.beanFactory = beanFactory;
    }

    // ========== 控制执行顺序（最低优先级，最后执行） ==========
    @Override
    public int getOrder() {
        // 确保所有Bean实例化完成后再执行扫描/注册
        return LOWEST_PRECEDENCE;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        // 1. 解析@EnableAutoFill注解属性
        AnnotationAttributes autoFillAttrs = AnnotationAttributes.fromMap(
                importingClassMetadata.getAnnotationAttributes(EnableAutoFill.class.getName()));
        if (autoFillAttrs == null) {
            return;
        }

        // 2. 解析扫描包（basePackageClasses > basePackages > 默认包）
        Set<String> scanPackages = resolveScanPackages(importingClassMetadata, autoFillAttrs);
        if (scanPackages.isEmpty()) {
            return;
        }

        // 3. 解析排除类
        Set<Class<?>> excludeClasses = resolveExcludeClasses(autoFillAttrs);

        // 4. 初始化BeanName生成器（安全：用BeanFactoryAware获取的beanFactory）
        BeanNameGenerator nameGenerator = resolveBeanNameGenerator(autoFillAttrs, registry);

        // 5. 扫描用户处理器并注册
        Set<Class<?>> userHandlers = scanFillHandlers(scanPackages, excludeClasses);
        registerHandlers(userHandlers, registry, nameGenerator);

        // 6. 注册框架默认处理器（排除指定类）
        if (autoFillAttrs.getBoolean("enableFrameworkFill")) {
            Set<Class<?>> defaultHandlers = DEFAULT_FRAMEWORK_HANDLERS.stream()
                    .filter(clazz -> !excludeClasses.contains(clazz))
                    .collect(Collectors.toSet());
            registerHandlers(defaultHandlers, registry, nameGenerator);
        }
    }

    // ========== 核心优化：安全解析BeanNameGenerator（无反射） ==========
    private BeanNameGenerator resolveBeanNameGenerator(AnnotationAttributes attrs, BeanDefinitionRegistry registry) {
        Class<? extends BeanNameGenerator> generatorClass = attrs.getClass("nameGenerator");
        BeanNameGenerator beanNameGenerator;
        // 步骤1：优先从容器中查找该类型的Bean（安全：用BeanFactoryAware的beanFactory）
        if (this.beanFactory != null) {
            ObjectProvider<? extends BeanNameGenerator> beanProvider = this.beanFactory.getBeanProvider(generatorClass);
            beanNameGenerator = beanProvider.getIfAvailable();
            if (Objects.nonNull(beanNameGenerator)) {
                return beanNameGenerator;
            }
        }

        // 步骤2：容器无该Bean，反射实例化（仅实例化生成器，无反射获取容器）
        try {
            return this.instantiateGeneratorWithConstructor(generatorClass, registry);
        } catch (Exception e) {
            // 步骤3：终极兜底，返回Spring默认实现
            log.warn("BeanName生成器初始化失败，使用默认生成器 AnnotationBeanNameGenerator");
            return new AnnotationBeanNameGenerator();
        }
    }

    /**
     * 实例化BeanNameGenerator（仅解析构造参数，无反射操作容器）
     */
    private BeanNameGenerator instantiateGeneratorWithConstructor(
            Class<? extends BeanNameGenerator> generatorClass,
            BeanDefinitionRegistry registry) throws Exception {

        // 优先public构造，再非public（仅反射生成器本身，无反射容器）
        Constructor<?>[] constructors = generatorClass.getConstructors();
        if (constructors.length == 0) {
            constructors = generatorClass.getDeclaredConstructors();
        }

        // 遍历构造函数，找可实例化的
        for (Constructor<?> constructor : constructors) {
            try {
                int modifiers = constructor.getModifiers();
                if (!Modifier.isPublic(modifiers)) {
                    continue;
                }
                Object[] args = resolveConstructorArgs(constructor.getParameterTypes(), registry);
                return (BeanNameGenerator) constructor.newInstance(args);
            } catch (Exception e) {
                // 该构造失败，尝试下一个
            }
        }

        throw new IllegalArgumentException("无法实例化BeanNameGenerator：" + generatorClass.getName());
    }

    /**
     * 解析构造参数
     *
     * @param paramTypes 参数类型
     * @param registry   spring register 对象
     * @return Object 所需的参数数组
     * @date 2025/12/29
     */
    private Object[] resolveConstructorArgs(Class<?>[] paramTypes, BeanDefinitionRegistry registry) {
        List<Object> args = new ArrayList<>();
        for (Class<?> paramType : paramTypes) {
            Object arg = null;

            // 步骤1：从容器取参数（安全：用BeanFactoryAware的beanFactory）
            if (this.beanFactory != null) {
                // 获取参数类型的ObjectProvider（仅1次BeanFactory调用）
                ObjectProvider<?> beanProvider = this.beanFactory.getBeanProvider(paramType);
                // 取第一个可用的Bean（等价于原逻辑：beanNames[0]），无则返回null
                arg = beanProvider.getIfAvailable();
            }

            // 步骤2：默认值（仅用已知上下文，无反射）
            if (arg == null) {
                arg = getDefaultArgForType(paramType, registry);
            }

            // 步骤3：基础类型默认值
            if (arg == null) {
               throw new RuntimeException("cannot found bean in factory by type with "+paramType);
            }

            args.add(arg);
        }
        return args.toArray();
    }
    /**
     * 为参数类型提供默认适配值（贴合Spring容器场景）
     */
    private Object getDefaultArgForType(Class<?> paramType, BeanDefinitionRegistry registry) {
        // BeanDefinitionRegistry参数：用当前registry
        if (BeanDefinitionRegistry.class.isAssignableFrom(paramType)) {
            return registry;
        }
        // ConfigurableListableBeanFactory参数：用当前容器
        if (ConfigurableListableBeanFactory.class.isAssignableFrom(paramType)) {
            return this.getBeanFactoryFromRegistry(registry);
        }
        // ClassLoader参数：用当前类加载器
        if (ClassLoader.class == paramType) {
            return ClassUtils.getDefaultClassLoader();
        }
        // 其他类型无默认值，返回null
        return null;
    }
    /**
     * 从BeanDefinitionRegistry中获取ConfigurableListableBeanFactory（容器核心）
     */
    private ConfigurableListableBeanFactory getBeanFactoryFromRegistry(BeanDefinitionRegistry registry) {
        if (registry instanceof ConfigurableListableBeanFactory) {
            return (ConfigurableListableBeanFactory) registry;
        }
        // Spring Boot/Context 中，Registry通常是DefaultListableBeanFactory的包装，兼容这种场景
        if (registry.getClass().getName().contains("DefaultListableBeanFactory")) {
            try {
                // 反射获取内部的beanFactory（兼容Spring不同版本）
                return (ConfigurableListableBeanFactory) registry.getClass()
                        .getMethod("getBeanFactory")
                        .invoke(registry);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
    /**
     * 解析构造参数（支持List/Map集合类型 + 单个对象类型）
     *
     * @param genericParamTypes 泛型参数类型（解决泛型擦除，解析List/Map的泛型类型）
     * @param rawParamTypes     原始参数类型（List/Map/单个对象）
     * @param registry          Spring注册器（仅占位）
     * @return 构造参数数组（仅从IOC容器获取，无则抛异常）
     * @throws IllegalArgumentException 任意参数获取失败时抛出
     */
    private Object[] resolveConstructorArgs(Type[] genericParamTypes, Class<?>[] rawParamTypes, BeanDefinitionRegistry registry) {
        if (this.beanFactory == null) {
            throw new IllegalArgumentException("BeanFactory未初始化，无法从IOC容器获取构造参数");
        }

        List<Object> args = new ArrayList<>();
        for (int i = 0; i < rawParamTypes.length; i++) {
            Class<?> rawType = rawParamTypes[i];
            Object arg;

            // 1. 处理List<T>类型参数：获取容器中所有T类型Bean，组装为List
            if (List.class.isAssignableFrom(rawType)) {
                arg = resolveListParam(rawType);
            }
            // 2. 处理Map<String, T>类型参数：获取容器中所有T类型Bean，组装为Map（key=beanName）
            else if (Map.class.isAssignableFrom(rawType)) {
                arg = resolveMapParam(rawType);
            }
            // 3. 处理单个对象类型参数：获取容器中第一个可用Bean
            else {
                arg = resolveSingleBeanParam(rawType);
            }

            // 任意参数获取不到（null），直接构造失败
            if (arg == null) {
                throw new IllegalArgumentException(
                        String.format("构造参数[%s]在IOC容器中未找到可用Bean，构造失败", rawType.getName())
                );
            }
            args.add(arg);
        }
        return args.toArray();
    }

    /**
     * 解析List<T>类型参数：从容器获取所有T类型Bean
     */
    private List<?> resolveListParam(Type genericType) {
        // 解析List的泛型类型（如List<Bean> → Bean.class）
        Class<?> elementType = resolveGenericElementType(genericType);
        if (elementType == null) {
            throw new IllegalArgumentException("List参数未指定泛型类型，无法解析");
        }

        // 获取容器中所有该类型的 Bean
        ObjectProvider<?> beanProvider = this.beanFactory.getBeanProvider(elementType);
        List<?> collectArg = beanProvider.stream().sorted().collect(Collectors.toList());
        if (CollectionUtil.isEmpty(collectArg)) {
            // 无Bean则返回null，触发构造失败
            return null;
        }
        // 组装为List
        return collectArg;
    }

    /**
     * 解析Map<String, T>类型参数：从容器获取所有T类型Bean（key=beanName，value=实例）
     */
    private Map<String, ?> resolveMapParam(Type genericType) {
        // 解析Map的value泛型类型（如Map<String, Bean> → Bean.class）
        Class<?> valueType = resolveGenericMapValueType(genericType, Map.class);
        if (valueType == null) {
            throw new IllegalArgumentException("Map参数未指定泛型类型（需为Map<String, T>），无法解析");
        }

        if (this.beanFactory instanceof ConfigurableListableBeanFactory listableBeanFactory) {
            // 获取容器中所有该类型的Bean
            String[] beanNames = listableBeanFactory.getBeanNamesForType(valueType);
            if (beanNames.length == 0) {
                return null; // 无Bean则返回null，触发构造失败
            }

            // 组装为Map（key=beanName，value=Bean实例）
            Map<String, Object> beanMap = new HashMap<>();
            for (String beanName : beanNames) {
                beanMap.put(beanName, this.beanFactory.getBean(beanName, valueType));
            }
            return beanMap;
        }
        ObjectProvider<?> beanProvider = this.beanFactory.getBeanProvider(valueType);
        return beanProvider.stream()
                .collect(Collectors.toMap(item -> StrUtil.toCamelCase(item.getClass().getSimpleName()), Function.identity()));
    }

    /**
     * 解析单个对象类型参数：从容器获取第一个可用Bean
     */
    private Object resolveSingleBeanParam(Class<?> paramType) {
        ObjectProvider<?> provider = this.beanFactory.getBeanProvider(paramType);
        // 无则返回null，触发构造失败
        return provider.getIfAvailable();
    }

    /**
     * 解析List的泛型元素类型（解决泛型擦除）
     */
    private Class<?> resolveGenericElementType(Type genericType) {
        if (!(genericType instanceof ParameterizedType paramType)) {
            return null;
        }

        Type[] actualTypeArguments = paramType.getActualTypeArguments();
        if (actualTypeArguments.length != 1) {
            return null;
        }

        Type elementType = actualTypeArguments[0];
        return elementType instanceof Class ? (Class<?>) elementType : null;
    }

    /**
     * 解析Map的value泛型类型（解决泛型擦除，仅支持Map<String, T>）
     */
    private Class<?> resolveGenericMapValueType(Type genericType, Class<?> rawType) {
        if (!(genericType instanceof ParameterizedType)) {
            return null;
        }

        ParameterizedType paramType = (ParameterizedType) genericType;
        Type[] actualTypeArguments = paramType.getActualTypeArguments();
        // Map<String, T> 需满足：key=String，value=T
        if (actualTypeArguments.length != 2 || !(actualTypeArguments[0] instanceof Class) || !String.class.equals(actualTypeArguments[0])) {
            return null;
        }

        Type valueType = actualTypeArguments[1];
        return valueType instanceof Class ? (Class<?>) valueType : null;
    }


    // ========== 其他核心方法（无修改，纯安全逻辑） ==========
    private void registerHandlers(Set<Class<?>> handlerClasses, BeanDefinitionRegistry registry, BeanNameGenerator nameGenerator) {
        for (Class<?> handlerClass : handlerClasses) {
            if (handlerClass.isInterface() || Modifier.isAbstract(handlerClass.getModifiers())) {
                continue;
            }
            AnnotatedGenericBeanDefinition beanDefinition = new AnnotatedGenericBeanDefinition(handlerClass);
            String beanName = nameGenerator.generateBeanName(beanDefinition, registry);
            if (!registry.containsBeanDefinition(beanName)) {
                registry.registerBeanDefinition(beanName, beanDefinition);
            }
        }
    }

    private Set<String> resolveScanPackages(AnnotationMetadata metadata, AnnotationAttributes attrs) {
        Set<String> scanPackages = new LinkedHashSet<>();
        Class<?>[] basePackageClasses = attrs.getClassArray("basePackageClasses");
        if (basePackageClasses.length > 0) {
            for (Class<?> clazz : basePackageClasses) {
                scanPackages.add(ClassUtils.getPackageName(clazz));
            }
            return scanPackages;
        }
        String[] basePackages = attrs.getStringArray("basePackages");
        if (basePackages.length > 0) {
            scanPackages.addAll(Arrays.asList(basePackages));
            return scanPackages;
        }
        scanPackages.add(ClassUtils.getPackageName(metadata.getClassName()));
        return scanPackages;
    }

    private Set<Class<?>> resolveExcludeClasses(AnnotationAttributes attrs) {
        return new HashSet<>(Arrays.asList(attrs.getClassArray("excludeClasses")));
    }

    private Set<Class<?>> scanFillHandlers(Set<String> scanPackages, Set<Class<?>> excludeClasses) {
        Set<Class<?>> handlerClasses = new LinkedHashSet<>();
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false) {
            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                return beanDefinition.getMetadata().isConcrete() && !beanDefinition.getMetadata().isAbstract();
            }
        };
        for (Class<?> targetInterface : TARGET_INTERFACES) {
            scanner.addIncludeFilter(new AssignableTypeFilter(targetInterface));
        }
        if (!excludeClasses.isEmpty()) {
            scanner.addExcludeFilter((metadataReader, metadataReaderFactory) -> {
                String className = metadataReader.getClassMetadata().getClassName();
                return excludeClasses.stream().anyMatch(clazz -> clazz.getName().equals(className));
            });
        }
        for (String packageName : scanPackages) {
            if (!StringUtils.hasText(packageName)) continue;
            scanner.findCandidateComponents(packageName).forEach(candidate -> {
                try {
                    String className = candidate.getBeanClassName();
                    if (className != null) {
                        handlerClasses.add(ClassUtils.forName(className, ClassUtils.getDefaultClassLoader()));
                    }
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("加载填充处理器失败：" + candidate.getBeanClassName(), e);
                }
            });
        }
        return handlerClasses;
    }
}
