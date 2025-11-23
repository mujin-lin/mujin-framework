package com.mujin.commons.csv.handler;

import com.mujin.commons.csv.config.BoolSupplierConfig;
import com.mujin.commons.csv.entry.FieldCacheEntry;
import com.mujin.commons.csv.enums.CsvHandlerEnum;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 基础数据处理handler抽象类，抽象的目的是为了兼容reader和writer的getHandlerType
 * <br/>
 * 同时能够处理boolean类型
 *
 * @author chenglin.wu
 */
public abstract class CsvBasicAbstractHandler implements CsvReadHandler, CsvWriteHandler {

    @Override
    public CsvHandlerEnum getHandlerType() {
        return CsvHandlerEnum.BASIC;
    }

    @Override
    public <T> void readCsvData(CsvHandlerEnum csvHandlerEnum, FieldCacheEntry fieldCacheEntry, String columData, T tObject, Function<String, Boolean> boolFunction) throws InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException {
        if (fieldCacheEntry.isBoolean()) {
            this.readBoolean(fieldCacheEntry, columData, tObject, boolFunction);
            return;
        }
        this.readData(fieldCacheEntry, columData, tObject);
    }

    @Override
    public void writeData(CsvHandlerEnum csvHandlerEnum, FieldCacheEntry cacheEntry, StringBuilder stringBuilder, Object classFieldValue, Supplier<BoolSupplierConfig> boolTranStrSupplier) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        if ( cacheEntry.isBoolean()) {
            this.writeBoolean(cacheEntry, stringBuilder, classFieldValue, boolTranStrSupplier);
            return;
        }
        this.writeData(cacheEntry, stringBuilder, classFieldValue);
    }

    /**
     * 读取boolean值
     *
     * @param fieldCacheEntry 缓存entry
     * @param columData       当前列的数据
     * @param tObject         目标对象
     * @param boolFunction    字符串转boolean的方法
     * @date 2025/11/23
     */
    public <T> void readBoolean(FieldCacheEntry fieldCacheEntry, String columData, T tObject, Function<String, Boolean> boolFunction) throws InvocationTargetException, IllegalAccessException {

    }

    /**
     * 读取数据
     *
     * @param fieldCacheEntry 缓存entry
     * @param columData       当前列的数据
     * @param tObject         目标对象
     * @date 2025/11/23
     */
    public <T> void readData(FieldCacheEntry fieldCacheEntry, String columData, T tObject) throws InvocationTargetException, IllegalAccessException {

    }

    /**
     * 写出boolean值
     *
     * @param cacheEntry          cacheEntry
     * @param stringBuilder       stringBuilder
     * @param classFieldValue     classFieldValue
     * @param boolTranStrSupplier boolTranStrSupplier
     * @date 2025/11/23
     */
    public void writeBoolean(FieldCacheEntry cacheEntry, StringBuilder stringBuilder, Object classFieldValue, Supplier<BoolSupplierConfig> boolTranStrSupplier) {

    }

    /**
     * 写出其他的基础类型值
     *
     * @param cacheEntry      cacheEntry
     * @param stringBuilder   stringBuilder
     * @param classFieldValue classFieldValue
     * @date 2025/11/23
     */
    public void writeData(FieldCacheEntry cacheEntry, StringBuilder stringBuilder, Object classFieldValue) {

    }
}
