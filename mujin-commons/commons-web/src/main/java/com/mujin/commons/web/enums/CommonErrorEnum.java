package com.mujin.commons.web.enums;



/**
 * 通用异常枚举类
 *
 * @author chenglin.wu
 * @date 2025/11/23
 */
public enum CommonErrorEnum {
    /**
     * 请求处理成功
     */
    SUCCESS(200),
    /**
     * AUTH: 401未授权
     */
    UNAUTHORIZED(401),
    /**
     * 登录过期
     */
    LOGIN_EXPIRED(402),
    /**
     * 强制下线
     */
    FORCED_OFFLINE(403),
    /**
     * 404 未知
     */
    UNKNOWN(404),
    /**
     * 请求来源异常
     */
    REQUEST_SOURCE(405),
    /**
     * DATACHECK: 406未满足数据校验
     */
    DATA_CHECK(406),
    /**
     * 访问受限
     */
    LIMITED_ACCESS(407),
    /**
     * 业务异常
     */
    BUSINESS(9990),
    /**
     * THROWABLE: 9999程序异常
     */
    THROWABLE(9999);
    /**
     * 错误代码
     */
    private final int code;

    CommonErrorEnum(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }
}
