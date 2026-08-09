package com.mujin.commons.lang.code;

/**
 * 服务器异常
 *
 * @author chenglin.wu
 * @date 2025/12/6
 */
public enum BaseErrorCode implements ErrorCodeDefinition {
    /**
     * 服务器异常，但异常未知
     */
    UNKNOWN_ERROR(500),
    /**
     * 业务系统异常
     */
    BUSINESS_ERROR(900),
    /**
     * 框架内部异常
     */
    FRAMEWORK_ERROR(700),
    /**
     * 系统崩溃
     */
    CRASH_ERROR(9000);
    /**
     * 异常码
     */
    private final int errorCode;

    BaseErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public int errorCode() {
        return this.errorCode;
    }
}
