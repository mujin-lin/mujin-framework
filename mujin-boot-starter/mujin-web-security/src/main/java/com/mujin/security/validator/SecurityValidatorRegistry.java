package com.mujin.security.validator;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * 安全验证逻辑注册器
 *
 * @author chenglin.wu
 * @date 2025/12/6
 */
@Getter
public class SecurityValidatorRegistry {
    /**
     * 注册的结果
     */
    private final List<SecurityValidatorRegistration> registrations = new ArrayList<>();

    /**
     * 添加验证器
     *
     * @param securityValidator 安全验证
     * @return SecurityValidatorRegistration
     * @author chenglin.wu
     * @date 2025/12/07
     */
    private SecurityValidatorRegistration addValidator(SecurityValidator securityValidator) {
        SecurityValidatorRegistration registration = new SecurityValidatorRegistration(securityValidator);
        registrations.add(registration);
        return registration;
    }
}
