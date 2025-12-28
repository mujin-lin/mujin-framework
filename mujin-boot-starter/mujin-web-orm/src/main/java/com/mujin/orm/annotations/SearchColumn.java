package com.mujin.orm.annotations;

import java.lang.annotation.*;

/**
 * 在模糊搜索的接收类中打上当前注解，将当前注解的属性转换成对应的列名，从而构造queryWrapper
 *
 * @author chenglin.wu
 * @date 2025/12/27 21:31
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
public @interface SearchColumn {
    /**
     * 搜索的列名
     *
     * @return String 列名
     * @date 2025/12/27
     */
    String value() default "";

    /**
     * 是否构造为搜索条件
     * 默认 true 构造，false 不构造
     *
     * @return boolean
     * @date 2025/12/27
     */
    boolean exist() default true;
}
