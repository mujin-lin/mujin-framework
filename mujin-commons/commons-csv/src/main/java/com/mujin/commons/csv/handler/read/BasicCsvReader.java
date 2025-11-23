package com.mujin.commons.csv.handler.read;


import cn.hutool.core.convert.Convert;
import com.mujin.commons.csv.entry.FieldCacheEntry;
import com.mujin.commons.csv.handler.CsvBasicAbstractHandler;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;

/**
 * 基础数据处理handler
 *
 * @author chenglin.wu
 */
public class BasicCsvReader extends CsvBasicAbstractHandler {

    @Override
    public <T> void readData(FieldCacheEntry fieldCacheEntry, String columData, T tObject) throws InvocationTargetException, IllegalAccessException {
        // 如果当前列对应的列数据类型为基础类型，则当前 cache entry 中的缓存属性class就为对应的类型
        fieldCacheEntry.getFieldSetter().invoke(tObject, Convert.convert(fieldCacheEntry.getFieldClass(), columData));
    }

    @Override
    public <T> void readBoolean(FieldCacheEntry fieldCacheEntry, String columData, T tObject, Function<String, Boolean> boolFunction) throws InvocationTargetException, IllegalAccessException {
        Boolean aBoolean = boolFunction.apply(columData);
        fieldCacheEntry.getFieldSetter().invoke(tObject, aBoolean);
    }
}
