package com.mujin.orm;


import cn.hutool.core.util.StrUtil;
import com.mujin.orm.annotations.EnableAutoFill;
import com.mujin.orm.handler.InsertFillColumnHandler;
import com.mujin.orm.handler.UpdateFillColumnHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.lang.NonNull;
import org.springframework.util.ClassUtils;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * 自动注入类的register,用于查找接口<br/>
 * InsertFillColumnHandler 和 UpdateFillColumnHandler 对应的实现类
 *
 * @author chenglin.wu
 * @date 2025/12/27 22:22
 */
@Slf4j
public class AutoFillRegister implements ImportBeanDefinitionRegistrar, EnvironmentAware, ResourceLoaderAware {

    private Environment environment;

    private ResourceLoader resourceLoader;

    @Override
    public void setResourceLoader(@NonNull ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void setEnvironment(@NonNull Environment environment) {
        this.environment = environment;
    }

    @Override
    public void registerBeanDefinitions(@NonNull AnnotationMetadata annotationMetadata, @NonNull BeanDefinitionRegistry beanDefinitionRegistry) {
        Set<String> basePackages = getBasePackages(annotationMetadata);
        if (basePackages.isEmpty()) {
            return;
        }
        Set<BeanDefinition> candidateComponents = new LinkedHashSet<>();
        ClassPathScanningCandidateComponentProvider scanner = this.getScanner();
        scanner.setResourceLoader(this.resourceLoader);
        scanner.addIncludeFilter(new AssignableTypeFilter(InsertFillColumnHandler.class));
        scanner.addIncludeFilter(new AssignableTypeFilter(UpdateFillColumnHandler.class));
        for (String basePackage : basePackages) {
            candidateComponents.addAll(scanner.findCandidateComponents(basePackage));
        }
        if (candidateComponents.isEmpty()) {
            return;
        }
        Set<String> registerBeanClassName = new HashSet<>();
        // 注册处
        for (BeanDefinition candidateComponent : candidateComponents) {
            String beanClassName = candidateComponent.getBeanClassName();
            if (registerBeanClassName.contains(beanClassName) || StrUtil.isBlank(beanClassName)) {
                continue;
            }
            registerBeanClassName.add(beanClassName);
            try {
                Class<?> beanClass = Class.forName(beanClassName);
                boolean anInterface = beanClass.isInterface();
                if (anInterface) {
                    continue;
                }
                BeanDefinitionBuilder definition = BeanDefinitionBuilder.genericBeanDefinition(beanClass);
                definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);

                AbstractBeanDefinition beanDefinition = definition.getBeanDefinition();
                beanDefinition.setPrimary(true);
                beanDefinition.setAttribute(FactoryBean.OBJECT_TYPE_ATTRIBUTE, beanClass);
                BeanDefinitionHolder holder = new BeanDefinitionHolder(beanDefinition, beanClassName);
                BeanDefinitionReaderUtils.registerBeanDefinition(holder, beanDefinitionRegistry);
            } catch (ClassNotFoundException e) {
                log.warn("Cannot found class name:{}", beanClassName);
            }

        }

    }


    /**
     * 获取扫描的基础jar包
     *
     * @param importingClassMetadata 注解元数据信息
     * @return Set<String>
     * @date 2025/12/27
     */
    private Set<String> getBasePackages(AnnotationMetadata importingClassMetadata) {
        Map<String, Object> attributes = importingClassMetadata
                .getAnnotationAttributes(EnableAutoFill.class.getCanonicalName());

        Set<String> basePackages = new HashSet<>();
        // 是否有排除当前框架内的包
        boolean innerPackageFlag = false;

        for (String pkg : (String[]) attributes.get("value")) {
            if (StrUtil.isNotBlank(pkg)) {
                basePackages.add(pkg);
            }
        }
        for (String pkg : (String[]) attributes.get("basePackages")) {
            if (StrUtil.isNotBlank(pkg)) {
                basePackages.add(pkg);
            }
        }
        for (String pkg : (String[]) attributes.get("excludePackages")) {
            if ("com.sjchn.orm.handler".equals(pkg)) {
                innerPackageFlag = true;
            }
            if (StrUtil.isNotBlank(pkg)) {
                basePackages.remove(pkg);
            }
        }

        if (basePackages.isEmpty()) {
            if (!innerPackageFlag) {
                basePackages.add("com.sjchn.orm.handler");
            }
            basePackages.add(ClassUtils.getPackageName(importingClassMetadata.getClassName()));
        }
        return basePackages;
    }

    private ClassPathScanningCandidateComponentProvider getScanner() {

        return new ClassPathScanningCandidateComponentProvider(false, this.environment) {
            @Override
            protected boolean isCandidateComponent(@NonNull AnnotatedBeanDefinition beanDefinition) {

                boolean isCandidate = false;
                if (beanDefinition.getMetadata().isIndependent()) {
                    if (!beanDefinition.getMetadata().isAnnotation()) {
                        isCandidate = true;
                    }
                }
                return isCandidate;
            }
        };
    }

}
