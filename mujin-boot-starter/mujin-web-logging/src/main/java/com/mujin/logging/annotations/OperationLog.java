package com.mujin.logging.annotations;

import java.lang.annotation.*;

import java.lang.annotation.*;

/**
 * 方法级操作日志注解
 * <br/>
 * 标注在方法上后，将自动采集入参、出参、耗时、异常与 Web 上下文，
 * 通过 {@code mujin.logging.storage-type} 配置写入数据库 / 文件 / Kafka。
 *
 * @author chenglin.wu
 * @date 2026/08/08
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {

    /**
     * 操作描述
     *
     * @return String 中文描述
     */
    String value();

    /**
     * SpEL 表达式：操作对象标识
     * <br/>
     * 示例：{@code "#req.orderId"} 或 {@code "#userId"}
     *
     * @return String SpEL 表达式
     */
    String bizId() default "";

    /**
     * SpEL 表达式：操作人标识
     * <br/>
     * 留空时优先取登录上下文中的用户名
     *
     * @return String SpEL 表达式
     */
    String operator() default "";

    /**
     * 是否保存入参
     *
     * @return boolean 默认 true
     */
    boolean saveParam() default true;

    /**
     * 是否保存出参
     *
     * @return boolean 默认 true
     */
    boolean saveResult() default true;

    /**
     * 慢方法阈值（毫秒），超过则单独标记
     *
     * @return long 默认 3000ms
     */
    long slowThreshold() default 3000L;
}