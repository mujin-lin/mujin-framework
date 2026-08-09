package com.mujin.commons.csv.annotations;

import java.lang.annotation.*;

/**
 * 泛型属性读取写出标志
 *
 * @author chenglin.wu
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CsvGenerics {
    /**
     * 泛型实际对象<br/>
     * 如果当前类中的属性是数组的话则不需要设置当前class，只需要关注主类就行
     *
     * @return Class<?>
     * @date 2025/11/23
     */
    Class<?> elementClass() default String.class;

    /**
     * 主类将泛型类添加进去的方法，默认会将泛型类整个对象加入到主类中，类似集合的操作<br/>
     * 当前方法只能有一个参数且为 element class 类型
     *
     * @return String
     * @date 2025/11/23
     */
    String mainSetterMethod() default "";

    /**
     * 泛型类将当前数据读取出来的方法设置到当前类中<br/>
     * 当前方法只能有一个参数且为String 类型<br/>
     * 如果当前类中的属性是数组的话则不需要设置当前方法名。主需要关注主类
     *
     * @return String
     * @date 2025/11/23
     */
    String elementSetterMethod() default "";


}
