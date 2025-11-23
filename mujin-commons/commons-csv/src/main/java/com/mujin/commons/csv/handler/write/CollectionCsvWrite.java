package com.mujin.commons.csv.handler.write;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import com.mujin.commons.csv.config.BoolSupplierConfig;
import com.mujin.commons.csv.entry.FieldCacheEntry;
import com.mujin.commons.csv.enums.CsvHandlerEnum;
import com.mujin.commons.csv.exception.CsvWriteException;
import com.mujin.commons.csv.handler.CsvCollectionAbstractHandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 集合类型、数组类型属性处理
 *
 * @author chenglin.wu
 * @date 2025/11/23
 */
//@SuppressWarnings("All")
public class CollectionCsvWrite extends CsvCollectionAbstractHandler {

    @Override
    public void writeData(CsvHandlerEnum csvHandlerEnum, FieldCacheEntry cacheEntry, StringBuilder stringBuilder, Object classFieldValue, Supplier<BoolSupplierConfig> boolTranStrSupplier) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        // 集合
        Method csvValueMethod = cacheEntry.getValueMethod();
        Collection<?> collection;
        if (cacheEntry.isArray()) {
            collection = Arrays.stream((Object[]) classFieldValue).collect(Collectors.toList());
        } else {
            collection = (Collection<?>) classFieldValue;
        }
        if (CollectionUtil.isEmpty(collection)) {
            return;
        }
        stringBuilder.append("\"");

        Class<?> genericsSubClass = cacheEntry.getGenericsSubClass();
        boolean genericsBoolean = false;
        for (Object o : collection) {
            // 集合泛型class是否为空
            if (Objects.isNull(genericsSubClass)) {
                genericsSubClass = o.getClass();
                cacheEntry.setGenericsSubClass(genericsSubClass);
                genericsBoolean = Boolean.class.equals(genericsSubClass);
            }

            if (Objects.isNull(csvValueMethod)) {
                csvValueMethod = o.getClass().getMethod(cacheEntry.getGetterInvokeMethodVal());
                cacheEntry.setValueMethod(csvValueMethod);
            }
            // 获取数据
            Object invoke = csvValueMethod.invoke(o);
            // 判断是否是boolean类型
            if (genericsBoolean) {
                // 转boolean值
                boolean aBoolean = BooleanUtil.toBoolean(StrUtil.toString(invoke));
                if (Objects.isNull(boolTranStrSupplier)) {
                    throw new CsvWriteException("boolean transformation fail");
                }
                // 获取自定义的boolean字符串
                BoolSupplierConfig boolSupplierConfig = boolTranStrSupplier.get();
                // 转换
                invoke = BooleanUtil.toString(aBoolean, boolSupplierConfig.getTrueValue(), boolSupplierConfig.getFalseValue(), boolSupplierConfig.getDefaultValue());
            }
            stringBuilder.append(invoke).append(StrUtil.COMMA);
        }
        stringBuilder.replace(stringBuilder.length() - 1, stringBuilder.length(), "\"").append(StrUtil.COMMA);
    }
}
