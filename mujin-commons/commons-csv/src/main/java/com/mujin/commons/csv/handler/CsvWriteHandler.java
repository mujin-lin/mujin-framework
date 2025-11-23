package com.mujin.commons.csv.handler;


import com.mujin.commons.csv.config.BoolSupplierConfig;
import com.mujin.commons.csv.entry.FieldCacheEntry;
import com.mujin.commons.csv.enums.CsvHandlerEnum;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Supplier;

/**
 * csv 写出数据接口
 *
 * @author chenglin.wu
 */
public interface CsvWriteHandler extends CsvHandler {
    /**
     * 写出数据的方法
     *
     * @param csvHandlerEnum      csv处理的方法的枚举
     * @param cacheEntry          缓存的entry
     * @param stringBuilder       结果builder
     * @param classFieldValue     对象的字段值
     * @param boolTranStrSupplier 布尔值转换的配置
     * @date 2025/11/23
     */
    void writeData(CsvHandlerEnum csvHandlerEnum, FieldCacheEntry cacheEntry, StringBuilder stringBuilder, Object classFieldValue, Supplier<BoolSupplierConfig> boolTranStrSupplier) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException;
}
