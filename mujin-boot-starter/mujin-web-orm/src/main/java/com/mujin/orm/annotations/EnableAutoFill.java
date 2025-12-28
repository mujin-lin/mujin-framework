package com.mujin.orm.annotations;

import com.mujin.orm.AutoFillRegister;
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
@Import(AutoFillRegister.class)
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
     * 排除掉不扫描的包名
     *
     * @return String[]
     */
    String[] excludePackages() default "";
}
