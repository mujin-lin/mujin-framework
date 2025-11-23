package com.mujin.commons.csv.handler.read;


import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.StrUtil;
import com.mujin.commons.csv.constants.CsvHandlerConstants;
import com.mujin.commons.csv.entry.FieldCacheEntry;
import com.mujin.commons.csv.enums.CsvHandlerEnum;
import com.mujin.commons.csv.handler.CsvJsonAbstractHandler;
import com.mujin.commons.lang.JsonUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;

/**
 * json 格式化类型处理
 *
 * @author chenglin.wu
 * @date 2025/11/23
 */
public class JsonFormatterCsvReader extends CsvJsonAbstractHandler {


    @Override
    public <T> void readCsvData(CsvHandlerEnum csvHandlerEnum, FieldCacheEntry fieldCacheEntry, String columData, T tObject, Function<String, Boolean> boolFunction) throws InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException {
        if (StrUtil.isBlank(columData)) {
            return;
        }
        String tempData = columData;
        if (columData.startsWith("\"")) {
            tempData = columData.substring(1, columData.length() - 2);
        }
        if (StrUtil.isBlank(tempData)) {
            return;
        }
        String realData = tempData.replace(CsvHandlerConstants.CSV_STR_REPLACE_QUOTE, "\"").replaceAll(StrPool.UNDERLINE, StrPool.COMMA);
        Object object = JsonUtil.toObject(realData, fieldCacheEntry.getFieldClass());
        fieldCacheEntry.getFieldSetter().invoke(tObject, object);
    }
}
