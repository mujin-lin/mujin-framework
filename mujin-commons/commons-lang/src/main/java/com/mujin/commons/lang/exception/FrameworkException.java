package com.mujin.commons.lang.exception;


import com.mujin.commons.lang.code.ErrorCodeDefinition;

/**
 * 框架内异常
 *
 * @author chenglin.wu
 * @date 2025/11/20
 */
@SuppressWarnings("unused")
public class FrameworkException extends CommonsException {

    public FrameworkException(String errMsg) {
        super(errMsg);
    }

    public FrameworkException(ErrorCodeDefinition errCode, String errMsg) {
        super(errCode, errMsg);
    }

    public FrameworkException(ErrorCodeDefinition errCode, String errMsg, Throwable cause) {
        super(errCode, errMsg, cause);
    }

    public FrameworkException(Throwable cause) {
        super(cause);
    }
}
