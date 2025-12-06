package com.mujin.security.validator;

/**
 * 安全验证的 configurer
 *
 * @author chenglin.wu
 * @date 2025/12/6
 */
public interface SecurityValidatorConfigurer {


    /**
     * 安全验证逻辑注册器
     *
     * @param validatorRegistry 验证注册器
     * @author chenglin.wu
     * @date 2025/12/06
     */
    void registryValidator(SecurityValidatorRegistry validatorRegistry);
}
