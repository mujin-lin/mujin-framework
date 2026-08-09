package com.mujin.logging.annotations;

import java.lang.annotation.*;

/**
 * 字段级注解：标记该字段在写入操作日志时按指定策略脱敏
 *
 * @author chenglin.wu
 * @date 2026/08/08
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogMask {

    /**
     * 脱敏策略
     *
     * @return MaskType 默认 KEEP_HEAD
     */
    MaskType value() default MaskType.KEEP_HEAD;

    /**
     * 保留头部字符数（KEEP_HEAD / MIDDLE 生效）
     *
     * @return int 默认 3
     */
    int head() default 3;

    /**
     * 保留尾部字符数（KEEP_TAIL / MIDDLE 生效）
     *
     * @return int 默认 4
     */
    int tail() default 4;
}