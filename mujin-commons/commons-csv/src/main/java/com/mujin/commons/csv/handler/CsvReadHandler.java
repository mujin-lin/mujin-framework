package com.mujin.commons.csv.handler;


import com.mujin.commons.csv.entry.FieldCacheEntry;
import com.mujin.commons.csv.enums.CsvHandlerEnum;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;

/**
 * 当前将csv文件或者csv格式字符串读取成对应的目标类集合数据<br/>
 * 不支持扁平化对象
 *
 * @author chenglin.wu
 */
public interface CsvReadHandler extends CsvHandler {
    /**
     * 将字符串读取成对应的类型数据
     *
     * @param csvHandlerEnum  枚举类型
     * @param fieldCacheEntry 缓存的entry
     * @param columData       当前列的数据
     * @param tObject         对象
     * @param boolFunction    如果当前属性是boolean类型则通过当前数值获取对应的boolean类型，允许自定义
     * @date 2025/11/23
     */
    <T> void readCsvData(CsvHandlerEnum csvHandlerEnum, FieldCacheEntry fieldCacheEntry, String columData, T tObject, Function<String, Boolean> boolFunction) throws InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException;
}
