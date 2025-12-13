package com.mujin.security.interceptor;

import com.mujin.commons.web.request.MjHttpRequestWrapper;
import com.mujin.security.properties.MjSecurityRequestProperties;
import com.mujin.security.validator.SecurityValidatorChain;
import com.mujin.security.validator.context.AfterHandlerValidatorContext;
import com.mujin.security.validator.context.PreHandleValidatorContext;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Objects;

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
    /**
     * 验证器的配置类
     */
    private final MjSecurityRequestProperties securityRequestProperties;
    /**
     * preHandle context 的本地线程变量
     */
    private final ThreadLocal<PreHandleValidatorContext> requestThreadLocal = new ThreadLocal<>();

    public ValidatorInterceptor(SecurityValidatorChain validatorChain, MjSecurityRequestProperties securityRequestProperties) {
        this.validatorChain = validatorChain;
        this.securityRequestProperties = securityRequestProperties;
    }

    @Override
    public boolean preHandle(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull Object handler) throws Exception {
        PreHandleValidatorContext preHandleValidatorContext = new PreHandleValidatorContext(this.wrapperRequest(request), response, handler);
        this.validatorChain.validateBefore(this.requestThreadLocal.get());
        this.requestThreadLocal.set(preHandleValidatorContext);
        return true;
    }

    @Override
    public void afterCompletion(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull Object handler, Exception ex) throws IOException {
        PreHandleValidatorContext preHandleValidatorContext = this.requestThreadLocal.get();
        this.requestThreadLocal.remove();
        if (Objects.isNull(preHandleValidatorContext)) {
            preHandleValidatorContext = new PreHandleValidatorContext(this.wrapperRequest(request), response, handler);
        }
        this.validatorChain.validateAfter(new AfterHandlerValidatorContext(preHandleValidatorContext, ex));
    }

    /**
     * 包装 request
     *
     * @param request 请求对象
     * @return HttpServletRequest
     * @date 2025/12/13
     */
    private HttpServletRequest wrapperRequest(HttpServletRequest request) throws IOException {
        return this.securityRequestProperties.isWrapperEnable() ? new MjHttpRequestWrapper(request) : request;
    }
}
