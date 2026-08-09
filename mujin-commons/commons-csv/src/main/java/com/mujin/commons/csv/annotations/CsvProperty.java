package com.mujin.commons.csv.annotations;

import java.lang.annotation.*;

/**
 * 注解当前类的属性，用于将当前类转换成 csv 文件格式的 字符串
 *
 * @author chenglin.wu
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CsvProperty {

    /**
     * header 名
     *
     * @return String
     * @date 2025/11/23
     */
    String value() default "";

    /**
     * 顺序
     *
     * @return int
     * @date 2025/11/23
     */
    int index() default Integer.MAX_VALUE;

    /**
     * 当前属性调用哪个方法转组成 CSV 数据 <br/>
     * 如果是集合则为集合泛型内部对象调用的方法
     *
     * @return String
     * @date 2025/11/23
     */
    String dataInvokeMethod() default "toString";

    /**
     * 将数据读取成类的时候调用当前属性的set方法<br/>
     * 当前方法应该为只有一个参数并且参数类型为String的方法
     *
     * @return String
     * @date 2025/11/23
     */
    String dataSetInvokeMethod() default "";

    /**
     * 是否将当前属性格式化成json格式进行分割<br/>
     * 如果此方法返回 true则不会去调用其他方法，<br/>
     * 会使用json直接转换或格式化成字符串
     *
     * @return boolean
     * @date 2025/11/23
     */
    boolean formatJson() default false;
}