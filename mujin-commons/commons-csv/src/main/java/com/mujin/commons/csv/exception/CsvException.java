package com.mujin.commons.csv.exception;


import com.mujin.commons.csv.enums.CsvErrorEnum;
import com.mujin.commons.lang.exception.CommonsException;

/**
 * csv处理时的可能会抛出的异常
 *
 * @author chenglin.wu
 */
public class CsvException extends CommonsException {


    public CsvException(String message) {
        super(CsvErrorEnum.CSV_ERROR, message);
    }

    public CsvException(CsvErrorEnum errorCode, String message) {
        super(errorCode, message);
    }

    public CsvException(String message, Throwable cause) {
        this(CsvErrorEnum.CSV_ERROR, message, cause);
    }

    public CsvException(CsvErrorEnum errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
