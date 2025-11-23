package com.mujin.commons.csv.handler.write;


import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import com.mujin.commons.csv.config.BoolSupplierConfig;
import com.mujin.commons.csv.entry.FieldCacheEntry;
import com.mujin.commons.csv.handler.CsvBasicAbstractHandler;

import java.util.function.Supplier;

/**
 * 基础数据处理handler
 * @author chenglin.wu
 */
public class BasicCsvWrite extends CsvBasicAbstractHandler {

    @Override
    public void writeBoolean(FieldCacheEntry cacheEntry, StringBuilder stringBuilder, Object classFieldValue, Supplier<BoolSupplierConfig> boolTranStrSupplier) {
        BoolSupplierConfig booleanTranStrDefault = boolTranStrSupplier.get();
        stringBuilder.append(BooleanUtil.toString((Boolean) classFieldValue, booleanTranStrDefault.getTrueValue(), booleanTranStrDefault.getFalseValue(), booleanTranStrDefault.getDefaultValue()));
        stringBuilder.append(StrUtil.COMMA);
    }

    @Override
    public void writeData(FieldCacheEntry cacheEntry, StringBuilder stringBuilder, Object classFieldValue) {
        if (classFieldValue instanceof String) {
            String str = (String)classFieldValue;
            if (str.contains(",") || str.contains("\"") || str.contains("\n") || str.contains("\r")) {
                // 添加双引号
                stringBuilder.append('"');
                // 替换双引号为两个双引号
                int len = str.length();
                for (int i = 0; i < len; i++) {
                    char c = str.charAt(i);
                    if (c == '"') {
                        stringBuilder.append('"');
                    }
                    stringBuilder.append(c);
                }
                // 添加双引号并关闭字符串
                stringBuilder.append('"');
            }else {
                stringBuilder.append(str);
            }

        }else {
            stringBuilder.append(classFieldValue);
        }

        stringBuilder.append(StrUtil.COMMA);
    }
}
