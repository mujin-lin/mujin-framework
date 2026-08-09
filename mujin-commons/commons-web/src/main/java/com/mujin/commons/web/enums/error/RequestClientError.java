package com.mujin.commons.web.enums.error;

import com.mujin.commons.lang.code.AuthorizationErrorCode;

/**
 * 请求客户端异常
 *
 * @author chenglin.wu
 * @date 2025/12/6
 */
public enum RequestClientError implements AuthorizationErrorCode {
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
     * 访问受限
     */
    LIMITED_ACCESS(407);

    private final int code;

    RequestClientError(int code) {
        this.code = code;
    }

    @Override
    public int errorCode() {
        return code;
    }
}
