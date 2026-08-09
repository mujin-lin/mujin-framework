package com.mujin.security.validator.context;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 验证器上下文抽象类
 *
 * @param request  请求对象
 * @param response 响应对象
 * @param handler  接口方法
 * @author chenglin.wu
 * @date 2025/12/13
 */
public record PreHandleValidatorContext(HttpServletRequest request, HttpServletResponse response,
                                        Object handler) implements ValidatorContext {

}
