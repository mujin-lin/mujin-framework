package com.mujin.commons.lang.exception;


/**
 * 业务异常
 *
 * @author chenglin.wu
 * @date 2021/4/16
 */
@SuppressWarnings("unused")
public class BusinessException extends CommonsException {

    public BusinessException(String errMsg) {
        this(9000,errMsg);
    }

    public BusinessException(Throwable cause) {
        this(9000,cause.getMessage(),cause);
    }

    public BusinessException(int errCode, String errMsg) {
        super(errCode, errMsg);
    }

    public BusinessException(String errMsg, Throwable cause) {
        this(9000,errMsg,cause);
    }

    public BusinessException(int errCode, String errMsg, Throwable e) {
        super(errCode,errMsg, e);
    }


}
