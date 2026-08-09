package com.mujin.commons.lang.exception;


import com.mujin.commons.lang.code.ErrorCodeDefinition;
import com.mujin.commons.lang.code.BaseErrorCode;

/**
 * 业务异常
 *
 * @author chenglin.wu
 * @date 2025/12/27
 */
@SuppressWarnings("unused")
public class BusinessException extends CommonsException {

    public BusinessException(String errMsg) {
        this(BaseErrorCode.BUSINESS_ERROR, errMsg);
    }

    public BusinessException(Throwable cause) {
        this(BaseErrorCode.BUSINESS_ERROR, cause.getMessage(), cause);
    }

    public BusinessException(ErrorCodeDefinition errCode, String errMsg) {
        super(errCode, errMsg);
    }

    public BusinessException(String errMsg, Throwable cause) {
        this(BaseErrorCode.BUSINESS_ERROR, errMsg, cause);
    }

    public BusinessException(ErrorCodeDefinition errCode, String errMsg, Throwable e) {
        super(errCode, errMsg, e);
    }


}
