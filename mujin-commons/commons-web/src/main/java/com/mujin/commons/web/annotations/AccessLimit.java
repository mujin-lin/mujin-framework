package com.mujin.commons.web.annotations;


import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * 访问限制注解
 *
 * @author chenglin.wu
 * @date 2025/11/23
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AccessLimit {
    /**
     * 秒
     *
     * @return 多少秒内
     */
    @AliasFor("second")
    long value() default 3L;

    /**
     * 设定时间内最大访问次数
     *
     * @return 最大访问次数
     */
    long maxVisits() default 10L;

    /**
     * 秒
     *
     * @return 多少秒内
     */
    @AliasFor("value")
    long second() default 3L;

    /**
     * 禁用时长，单位/秒
     *
     * @return 禁用时长
     */
    long forbiddenSecond() default 10L;
}