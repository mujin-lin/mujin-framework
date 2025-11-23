package com.mujin.commons.csv.exception;

/**
 * 读取csv 文件成指定类集合时的异常类
 *
 * @author chenglin.wu
 */
public class CsvReadException extends CsvException {

    public CsvReadException(String message) {
        super(101, message);
    }

    public CsvReadException(String message, Throwable cause) {
        super(101, message, cause);
    }
}
