package com.mujin.commons.csv.handler.read;


import com.mujin.commons.csv.entry.FieldCacheEntry;
import com.mujin.commons.csv.enums.CsvHandlerEnum;
import com.mujin.commons.csv.exception.CsvReadException;
import com.mujin.commons.csv.handler.CsvDateAbstractHandler;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;
import java.util.function.Function;

/**
 * 仅支持 LocalDateTime、LocalDate、Date类型
 *
 * @author chenglin.wu
 * @date 2025/11/23
 */
public class DateCsvReader extends CsvDateAbstractHandler {


    @Override
    public <T> void readCsvData(CsvHandlerEnum csvHandlerEnum, FieldCacheEntry cacheEntry, String columData, T tObject, Function<String, Boolean> boolFunction) throws InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException {
        String datePattern = cacheEntry.getDatePattern();
        TimeZone timeZone = cacheEntry.getTimeZone();
        Class<?> fieldClass = cacheEntry.getFieldClass();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(datePattern).withZone(timeZone.toZoneId());
        Object date;
        if (Date.class.equals(fieldClass)) {
            LocalDate dateTime = LocalDate.parse(columData, formatter);
            date = Date.from(dateTime.atTime(0, 0).atZone(timeZone.toZoneId()).toInstant());
        } else if (LocalDate.class.equals(fieldClass)) {
            date = LocalDate.parse(columData, formatter);
        } else if (LocalDateTime.class.equals(fieldClass)) {
            date = LocalDateTime.parse(columData, formatter);
        } else {
            throw new CsvReadException("unknown date type: " + fieldClass.getName());
        }
        cacheEntry.getFieldSetter().invoke(tObject, date);
    }
}
