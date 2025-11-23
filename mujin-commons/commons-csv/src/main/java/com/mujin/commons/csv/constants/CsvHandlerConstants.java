package com.mujin.commons.csv.constants;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * csv 处理需要用到的常量
 *  @author chenglin.wu
 */
public final class CsvHandlerConstants {

    private CsvHandlerConstants() {
    }

    /**
     * boolean方法获取值的前缀
     */
    public static final String IS_PREFIX = "is";
    /**
     * 布尔值为 true的字符串，默认
     */
    public static final String BOOLEAN_TRUE_NUM = "1";
    /**
     * 布尔值为 false的默认字符串
     */
    public static final String BOOLEAN_FALSE_NUM = "0";
    /**
     * csv 格式字符串替换英文逗号的字符串
     */
    public static final String CSV_STR_REPLACE_COMMA = "(SPLIT_UPPER)";
    /**
     * csv 格式字符串替换双引号的字符串
     */
    public static final String CSV_STR_REPLACE_QUOTE = "(AT@)";
    /**
     * 冒号
     */
    public static final String DOUBLE_QUOTE_STR = "\"";
    /**
     * json list 开始的中括号加冒号
     */
    public static final String JSON_LIST_START = DOUBLE_QUOTE_STR + "[";
    /**
     * json list 结束的中括号加冒号
     */
    public static final String JSON_LIST_END = "]" + DOUBLE_QUOTE_STR;
    /**
     * json 对象开始的花括号加冒号
     */
    public static final String JSON_OBJ_START = DOUBLE_QUOTE_STR + "{";
    /**
     * json 对象结束的花括号加冒号
     */
    public static final String JSON_OBJ_END = "}" + DOUBLE_QUOTE_STR;
    /**
     * 默认的toString方法名
     */
    public static final String DEFAULT_TO_STRING_METHOD_NAME = "toString";
    /**
     * String class 对象
     */
    public static final Class<String> STRING_CLASS_OBJ = String.class;
    /**
     * String class 对应的名字 String
     */
    public static final String STRING_CLASS_NAME = STRING_CLASS_OBJ.getSimpleName();

    /**
     * 试讲类型集合
     */
    private static final Set<Class<?>> DATE_TYPE_LIST = new HashSet<>();

    static {
        // 时间类型
        DATE_TYPE_LIST.add(LocalDate.class);
        DATE_TYPE_LIST.add(LocalDateTime.class);
        DATE_TYPE_LIST.add(Date.class);
    }

    /**
     * 是否是date类型
     *
     * @param clazz 检测目标class对象
     * @return boolean
     */
    public static boolean isDate(Class<?> clazz) {
        return DATE_TYPE_LIST.contains(clazz);
    }


}
