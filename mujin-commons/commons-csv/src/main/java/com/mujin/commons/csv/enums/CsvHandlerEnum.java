package com.mujin.commons.csv.enums;

/**
 * csv处理的枚举
 * @author chenglin.wu
 */
public enum CsvHandlerEnum {
    /**
     * 基本数据类型
     */
    BASIC,
    /**
     * 时间类型
     */
    DATE,
    /**
     * 集合类型
     */
    COLLECTION,
    /**
     * 是否格式化成json
     */
    FORMATTER_JSON,
    /**
     * 其他类
     */
    OTHER;

    /**
     * 根据传入条件获取枚举
     *
     * @param isBasic         基本数据类型
     * @param isDate          时间类型
     * @param isCollection    集合或者数组类型
     * @param isFormatterJson 是否格式化成json
     * @return CsvCreateValueEnum
     * @date 2025/11/23
     */
    public static CsvHandlerEnum getEnum(boolean isBasic, boolean isDate, boolean isCollection, boolean isFormatterJson) {
        if (isBasic) {
            return BASIC;
        }
        if (isDate) {
            return DATE;
        }
        if (isCollection) {
            return COLLECTION;
        }
        if (isFormatterJson) {
            return FORMATTER_JSON;
        }
        return OTHER;
    }
}
