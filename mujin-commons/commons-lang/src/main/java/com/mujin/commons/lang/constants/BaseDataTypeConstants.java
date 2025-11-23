package com.mujin.commons.lang.constants;

import java.util.HashSet;
import java.util.Set;

/**
 * 基础数据类型的常量对应名字
 *
 * @author chenglin.wu
 */
public final class BaseDataTypeConstants {

    private BaseDataTypeConstants() {
    }

    // ------------------------------------------------------------
    //                        基本数据类型
    // ------------------------------------------------------------
    /**
     * int
     */
    private static final String INT = "int";
    /**
     * char
     */
    private static final String CHAR = "char";
    /**
     * long
     */
    private static final String LONG = "long";
    /**
     * double
     */
    private static final String DOUBLE = "double";
    /**
     * float
     */
    private static final String FLOAT = "float";
    /**
     * boolean
     */
    private static final String BOOLEAN = "boolean";
    /**
     * byte
     */
    private static final String BYTE = "byte";
    /**
     * short
     */
    private static final String SHORT = "short";

    // ------------------------------------------------------------
    //                        包装类
    // ------------------------------------------------------------

    /**
     * Integer
     */
    private static final String PACKING_INT = "Integer";
    /**
     * Character
     */
    private static final String PACKING_CHAR = "Character";
    /**
     * Long
     */
    private static final String PACKING_LONG = "Long";
    /**
     * Double
     */
    private static final String PACKING_DOUBLE = "Double";
    /**
     * Float
     */
    private static final String PACKING_FLOAT = "Float";
    /**
     * Byte
     */
    private static final String PACKING_BYTE = "Byte";
    /**
     * Boolean
     */
    private static final String PACKING_BOOLEAN = "Boolean";
    /**
     * Short
     */
    private static final String PACKING_SHORT = "Short";


    /**
     * 保存当前数据的集合
     */
    private static final Set<String> DATA_TYPE_LIST = new HashSet<>(16);

    static {
        DATA_TYPE_LIST.add(BYTE);
        DATA_TYPE_LIST.add(INT);
        DATA_TYPE_LIST.add(CHAR);
        DATA_TYPE_LIST.add(LONG);
        DATA_TYPE_LIST.add(DOUBLE);
        DATA_TYPE_LIST.add(BOOLEAN);
        DATA_TYPE_LIST.add(FLOAT);
        DATA_TYPE_LIST.add(SHORT);
        DATA_TYPE_LIST.add(PACKING_BYTE);
        DATA_TYPE_LIST.add(PACKING_INT);
        DATA_TYPE_LIST.add(PACKING_CHAR);
        DATA_TYPE_LIST.add(PACKING_LONG);
        DATA_TYPE_LIST.add(PACKING_DOUBLE);
        DATA_TYPE_LIST.add(PACKING_FLOAT);
        DATA_TYPE_LIST.add(PACKING_SHORT);
        DATA_TYPE_LIST.add(PACKING_BOOLEAN);
    }

    /**
     * 获取基本数据类型的list
     *
     * @return Set<String>
     * @date 2025/11/23
     */
    @SuppressWarnings("unused")
    public static Set<String> getDataTypeList() {
        return DATA_TYPE_LIST;
    }

    /**
     * 是否是基本数据类型及其包装类
     *
     * @param typeName 类型名字
     * @return boolean
     * @date 2025/11/23
     */
    public static boolean containsType(String typeName) {
        return DATA_TYPE_LIST.contains(typeName);
    }

    /**
     * 判断是否是boolean类型或者其包装类
     *
     * @param typeName 数据类型的名字
     * @return boolean
     * @date 2025/11/23
     */
    public static boolean isBoolean(String typeName) {
        return BOOLEAN.equalsIgnoreCase(typeName);
    }


}
