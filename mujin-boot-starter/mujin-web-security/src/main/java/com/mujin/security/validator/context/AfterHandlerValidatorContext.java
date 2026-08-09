package com.mujin.security.validator.context;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;

/**
 * 请求处理后的 validator
 *
 * @author chenglin.wu
 * @date 2025/12/13
 */
@Getter
public class AfterHandlerValidatorContext implements AfterValidatorContext {

    /**
     * 异常信息
     */
    private final Exception exception;
    /**
     * 前置处理的 context
     */
    private final PreHandleValidatorContext preHandleValidatorContext;


    public AfterHandlerValidatorContext(HttpServletRequest request, HttpServletResponse response, Object handler) {
        this(request, response, handler, null);
    }

    public AfterHandlerValidatorContext(HttpServletRequest request, HttpServletResponse response, Object handler, Exception exception) {
        this(new PreHandleValidatorContext(request, response, handler), exception);
    }

    public AfterHandlerValidatorContext(PreHandleValidatorContext preHandleValidatorContext, Exception exception) {
        this.preHandleValidatorContext = preHandleValidatorContext;
        this.exception = exception;
    }

    @Override
    public HttpServletRequest request() {
        return this.preHandleValidatorContext.request();
    }

    @Override
    public HttpServletResponse response() {
        return this.preHandleValidatorContext.response();
    }

    @Override
    public Object handler() {
        return this.preHandleValidatorContext.handler();
    }
}
