package com.mujin.commons.web.annotations;

import java.lang.annotation.*;

/**
 * 接口是否需要登录后才能访问的检查注解，打上此注解在controller类上或者其对应的controller方法上进行验证
 *
 * @author chenglin.wu
 * @date 2025/11/23
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LoginCheck {
}