package com.mujin.commons.csv.exception;

import com.mujin.commons.csv.enums.CsvErrorEnum;

/**
 * 读取csv 文件成指定类集合时的异常类
 *
 * @author chenglin.wu
 */
public class CsvReadException extends CsvException {

    public CsvReadException(String message) {
        super(CsvErrorEnum.CSV_READ_ERROR, message);
    }

    public CsvReadException(String message, Throwable cause) {
        super(CsvErrorEnum.CSV_READ_ERROR, message, cause);
    }
}
