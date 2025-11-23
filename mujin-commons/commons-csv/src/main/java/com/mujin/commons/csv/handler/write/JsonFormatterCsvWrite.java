package com.mujin.commons.csv.handler.write;


import cn.hutool.core.util.StrUtil;
import com.mujin.commons.csv.config.BoolSupplierConfig;
import com.mujin.commons.csv.entry.FieldCacheEntry;
import com.mujin.commons.csv.enums.CsvHandlerEnum;
import com.mujin.commons.csv.handler.CsvJsonAbstractHandler;
import com.mujin.commons.lang.JsonUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Supplier;

/**
 * json 格式化类型处理
 *
 * @author chenglin.wu
 * @date 2025/11/23
 */
public class JsonFormatterCsvWrite extends CsvJsonAbstractHandler {


    @Override
    public void writeData(CsvHandlerEnum csvHandlerEnum, FieldCacheEntry cacheEntry, StringBuilder stringBuilder, Object classFieldValue, Supplier<BoolSupplierConfig> boolTranStrSupplier) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        String jsonStr = JsonUtil.toJson(classFieldValue);
        stringBuilder.append("\"").append(jsonStr).append("\"").append(StrUtil.COMMA);
    }
}
