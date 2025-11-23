package com.mujin.commons.csv.exception;

/**
 * 将指定数据集合写出成 csv 格式字符串或者文件的异常
 *
 * @author chenglin.wu
 */
public class CsvWriteException extends CsvException {

    public CsvWriteException(String message) {
        super(message);
    }
}
