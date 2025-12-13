package com.mujin.security.validator.context;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 验证器上下文
 *
 * @author chenglin.wu
 * @date 2025/12/13
 */
public interface ValidatorContext {
    /**
     * 请求对象
     *
     * @return HttpServletRequest
     * @date 2025/12/13
     */
    HttpServletRequest request();

    /**
     * 响应对象
     *
     * @return HttpServletResponse
     * @date 2025/12/13
     */
    HttpServletResponse response();

    /**
     * 处理请求的 handler
     *
     * @return Object
     * @date 2025/12/13
     */
    Object handler();
}
