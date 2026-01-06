package com.mujin.orm.annotations;

import com.mujin.orm.AutoFillComponentSelector;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * 是否需要开启当前框架的自动注入
 *
 * @author chenglin.wu
 * @date 2025/12/27 21:31
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(AutoFillComponentSelector.class)
public @interface EnableAutoFill {
    /**
     * 需要扫描的包
     *
     * @return String[]
     * @date 2025/12/27
     */
    @AliasFor("basePackages")
    String[] value() default "";

    /**
     * 需要扫描的包
     *
     * @return String[]
     * @date 2025/12/27
     */
    @AliasFor("value")
    String[] basePackages() default "";

    /**
     * 是否将框架内部的默认填充类注入容器
     *
     * @return boolean
     */
    boolean enableFrameworkFill() default true;


    /**
     * 通过类指定扫描包（优先级高于basePackages）
     * 示例：basePackageClasses = UserFillHandler.class → 扫描UserFillHandler所在包
     *
     * @return class 数组
     */
    Class<?>[] basePackageClasses() default {};

    /**
     * 排除不需要注册的填充处理器类
     *
     * @return class 数组
     */
    Class<?>[] excludeClasses() default {};

    /**
     * 自定义BeanName生成器（默认使用Spring注解BeanName生成器）
     *
     * @return like {@link BeanNameGenerator}
     */
    Class<? extends BeanNameGenerator> nameGenerator() default AnnotationBeanNameGenerator.class;
}
