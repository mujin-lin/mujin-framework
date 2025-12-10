package com.mujin.security.interceptor;

import com.mujin.security.validator.SecurityValidator;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

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
    private final List<SecurityValidator> validators;
    /**
     * 验证器数量
     */
    private final int validatorSize;

    public ValidatorInterceptor(List<SecurityValidator> validators) {
        this.validators = validators;
        this.validatorSize = validators.size();
    }

    @Override
    public boolean preHandle(@Nonnull final HttpServletRequest request, @Nonnull final HttpServletResponse response, @Nonnull final Object handler) throws Exception {
        for (int i = 0; i < this.validatorSize; i++) {
            this.validators.get(i).validateBefore();
        }
        return true;
    }

    @Override
    public void afterCompletion(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull Object handler, Exception ex) throws Exception {
        for (int i = this.validatorSize - 1; i >= 0; i--) {
            this.validators.get(i).validateAfter();
        }
    }
}
