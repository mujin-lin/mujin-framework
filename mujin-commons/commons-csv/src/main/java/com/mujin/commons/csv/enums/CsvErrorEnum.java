package com.mujin.commons.csv.enums;

import com.mujin.commons.lang.code.FrameworkErrorCode;

/**
 * 框架内 CSV 异常枚举
 *
 * @author chenglin.wu
 * @date 2025/12/7
 */
public enum CsvErrorEnum implements FrameworkErrorCode {
    /**
     * CSV异常
     */
    CSV_ERROR(701),
    /**
     * CSV 读取异常
     */
    CSV_READ_ERROR(702),
    /**
     * CSV 其他异常
     */
    CSV_WRITE_ERROR(703);


    private final int code;

    CsvErrorEnum(int code) {
        this.code = code;
    }

    @Override
    public int errorCode() {
        return this.code;
    }
}
