package com.mujin.commons.csv.handler.write;


import cn.hutool.core.util.StrUtil;
import com.mujin.commons.csv.config.BoolSupplierConfig;
import com.mujin.commons.csv.entry.FieldCacheEntry;
import com.mujin.commons.csv.enums.CsvHandlerEnum;
import com.mujin.commons.csv.handler.CsvDateAbstractHandler;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.function.Supplier;

/**
 * 仅支持 LocalDateTime、LocalDate、Date类型
 *
 * @author chenglin.wu
 * @date 2025/11/23
 */
public class DateCsvWrite extends CsvDateAbstractHandler {

    @Override
    public void writeData(CsvHandlerEnum csvHandlerEnum, FieldCacheEntry cacheEntry, StringBuilder stringBuilder, Object classFieldValue, Supplier<BoolSupplierConfig> boolTranStrSupplier) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        // 时间类型
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(cacheEntry.getDatePattern()).withZone(cacheEntry.getTimeZone().toZoneId());
        if (cacheEntry.isOldDateType()) {
            Date dateValue = (Date) classFieldValue;
            LocalDateTime localDateTime = LocalDateTime.ofInstant(dateValue.toInstant(), cacheEntry.getTimeZone().toZoneId());
            stringBuilder.append(formatter.format(localDateTime));
        } else {
            stringBuilder.append(formatter.format((TemporalAccessor) classFieldValue));
        }
        stringBuilder.append(StrUtil.COMMA);
    }
}
