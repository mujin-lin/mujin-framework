package com.mujin.commons.csv.handler.write;


import cn.hutool.core.util.StrUtil;
import com.mujin.commons.csv.config.BoolSupplierConfig;
import com.mujin.commons.csv.entry.FieldCacheEntry;
import com.mujin.commons.csv.enums.CsvHandlerEnum;
import com.mujin.commons.csv.handler.CsvOtherAbstractHandler;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Supplier;

/**
 * 其他类型，主要针对对象属性
 *
 * @author chenglin.wu
 * @date 2025/11/23
 */
public class OtherCsvWrite extends CsvOtherAbstractHandler {

    @Override
    public void writeData(CsvHandlerEnum csvHandlerEnum, FieldCacheEntry cacheEntry, StringBuilder stringBuilder, Object classFieldValue, Supplier<BoolSupplierConfig> boolTranStrSupplier) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        // 普通类
        String value = String.valueOf(cacheEntry.getValueMethod().invoke(classFieldValue));
        if (StrUtil.contains(value, StrUtil.COMMA)) {
            stringBuilder.append("\"").append(value).append(StrUtil.COMMA).append("\"").append(StrUtil.COMMA);
            return;
        }
        stringBuilder.append(value).append(StrUtil.COMMA);
    }
}
