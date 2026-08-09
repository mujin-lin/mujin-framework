package com.mujin.logging.annotations;

import java.lang.annotation.*;

/**
 * 字段级注解：标记该字段不写入操作日志
 *
 * @author chenglin.wu
 * @date 2026/08/08
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogIgnore {
}