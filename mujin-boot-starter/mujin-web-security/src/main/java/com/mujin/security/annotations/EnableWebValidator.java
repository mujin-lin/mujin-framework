package com.mujin.security.annotations;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 是否开启框架内部安全校验
 *
 * @author chenglin.wu
 * @date 2025/12/7
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(ValidatorConfigurationSelector.class)
public @interface EnableWebValidator {
}
