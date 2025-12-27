package com.mujin.commons.csv.handler.read;


import com.mujin.commons.csv.entry.FieldCacheEntry;
import com.mujin.commons.csv.enums.CsvHandlerEnum;
import com.mujin.commons.csv.handler.CsvOtherAbstractHandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.function.Function;

/**
 * 其他类型，主要针对对象属性
 *
 * @author chenglin.wu
 * @date 2025/11/23
 */
public class OtherCsvReader extends CsvOtherAbstractHandler {

    @Override
    public <T> void readCsvData(CsvHandlerEnum csvHandlerEnum, FieldCacheEntry cacheEntry, String columData, T tObject, Function<String, Boolean> boolFunction) throws InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException {
        Object newInstance = cacheEntry.getFieldClass().getConstructor().newInstance();
        if (Objects.isNull(cacheEntry.getGenericsSubClass())) {
            Method method = cacheEntry.getFieldClass().getMethod(cacheEntry.getSetterInvokeMethodVal(), String.class);
            method.invoke(newInstance, columData);
        } else {
            Object subInstance = cacheEntry.getGenericsSubClass().getConstructor().newInstance();
            cacheEntry.getGenericsSubSetterMethod().invoke(subInstance, columData);
            cacheEntry.getGenericsMainSetterMethod().invoke(newInstance, subInstance);
        }

        cacheEntry.getFieldSetter().invoke(tObject, newInstance);
    }
}
