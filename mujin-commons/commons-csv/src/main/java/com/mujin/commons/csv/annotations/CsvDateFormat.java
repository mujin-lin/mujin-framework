package com.mujin.commons.csv.annotations;

import java.lang.annotation.*;

/**
 * 时间格式化
 *
 * @author chenglin.wu
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CsvDateFormat {
    /**
     * 时间格式化类型
     *
     * @return String
     * @date 2025/11/23
     */
    String pattern() default "";
    /**
     * 时区
     * @return String
     * @date 2025/11/23
     */
    String timeZone() default "GM+8";
}
