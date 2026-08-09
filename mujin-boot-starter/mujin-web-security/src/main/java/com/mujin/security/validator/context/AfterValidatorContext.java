package com.mujin.security.validator.context;

/**
 * 接口处理完成后的上下文
 *
 * @author chenglin.wu
 * @date 2025/12/13
 */
public interface AfterValidatorContext extends ValidatorContext {
    /**
     * 处理的异常信息
     *
     * @return Exception
     * @date 2025/12/13
     */
    Exception getException();
}
