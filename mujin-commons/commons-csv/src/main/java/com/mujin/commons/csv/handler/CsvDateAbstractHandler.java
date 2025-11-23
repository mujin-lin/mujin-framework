package com.mujin.commons.csv.handler;


import com.mujin.commons.csv.config.BoolSupplierConfig;
import com.mujin.commons.csv.entry.FieldCacheEntry;
import com.mujin.commons.csv.enums.CsvHandlerEnum;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 时间类型handler抽象类，抽象的目的是为了兼容reader和writer的getHandlerType
 * @author chenglin.wu
 */
public abstract class CsvDateAbstractHandler implements CsvReadHandler, CsvWriteHandler {

    @Override
    public CsvHandlerEnum getHandlerType() {
        return CsvHandlerEnum.DATE;
    }

    @Override
    public <T> void readCsvData(CsvHandlerEnum csvHandlerEnum, FieldCacheEntry fieldCacheEntry, String columData, T tObject, Function<String, Boolean> boolFunction) throws InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException {

    }

    @Override
    public void writeData(CsvHandlerEnum csvHandlerEnum, FieldCacheEntry cacheEntry, StringBuilder stringBuilder, Object classFieldValue, Supplier<BoolSupplierConfig> boolTranStrSupplier) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {

    }
}
