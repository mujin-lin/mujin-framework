package com.mujin.commons.csv.annotations;

import java.lang.annotation.*;

/**
 * 当前类转换csv文件时忽略的属性
 *
 * @author chenglin.wu
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CsvIgnore {
}
