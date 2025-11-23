package com.mujin.commons.csv.exception;


import com.mujin.commons.lang.constants.IntConstants;
import com.mujin.commons.lang.exception.CommonsException;

/**
 * csv处理时的可能会抛出的异常
 *
 * @author chenglin.wu
 */
public class CsvException extends CommonsException {


    public CsvException(String message) {
        super(IntConstants.INT_100, message);
    }

    public CsvException(int errorCode, String message) {
        super(errorCode, message);
    }

    public CsvException(String message, Throwable cause) {
        this(IntConstants.INT_100, message, cause);
    }

    public CsvException(int errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
