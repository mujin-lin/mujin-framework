package com.mujin.commons.lang.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 常用异常
 *
 * @author chenglin.wu
 * @date 2025/11/20
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CommonsException extends RuntimeException {
    /**
     * 错误代码
     */
    private int errCode;

    /**
     * 错误信息
     */
    private String errMsg;

    public CommonsException(String errMsg) {
        this(1000, errMsg);
    }

    public CommonsException(int errCode, String errMsg) {
        super(errMsg);
        this.errCode = errCode;
        this.errMsg = errMsg;
    }

    public CommonsException(int errCode, String errMsg, Throwable cause) {
        super(errMsg, cause);
        this.errMsg = errMsg;
        this.errCode = errCode;
    }

    public CommonsException(Throwable cause) {
        super(cause);
        this.errCode = 1000;
        this.errMsg = cause.getMessage();
    }
}
