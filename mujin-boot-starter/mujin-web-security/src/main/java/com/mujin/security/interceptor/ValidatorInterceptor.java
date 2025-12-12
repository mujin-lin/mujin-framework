package com.mujin.security.interceptor;

import com.mujin.security.validator.SecurityValidatorChain;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 验证器的请求拦截
 *
 * @author chenglin.wu
 * @date 2025/12/7
 */
public class ValidatorInterceptor implements HandlerInterceptor {
    /**
     * 验证器
     */
    private final SecurityValidatorChain validatorChain;

    public ValidatorInterceptor(SecurityValidatorChain validatorChain) {
        this.validatorChain = validatorChain;
    }

    @Override
    public boolean preHandle(@Nonnull final HttpServletRequest request, @Nonnull final HttpServletResponse response, @Nonnull final Object handler) throws Exception {
        this.validatorChain.validateBefore();
        return true;
    }

    @Override
    public void afterCompletion(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull Object handler, Exception ex) throws Exception {
        this.validatorChain.validateAfter();
    }

}
