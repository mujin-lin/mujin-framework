package com.mujin.security.validator;

import com.mujin.security.validator.context.AfterHandlerValidatorContext;
import com.mujin.security.validator.context.PreHandleValidatorContext;

/**
 * 校验器
 *
 * @author chenglin.wu
 * @date 2025/12/6
 */
public interface SecurityValidator {

    /**
     * 请求到达前验证器
     *
     * @date 2025/12/06
     */
    void validateBefore(PreHandleValidatorContext context);

    /**
     * 请求执行完成释放资源等
     *
     * @date 2025/12/06
     */
    void validateAfter(AfterHandlerValidatorContext context);
}
