package com.mujin.security.validator;

import lombok.Getter;

/**
 * 校验器注册包装类
 *
 * @author chenglin.wu
 * @date 2025/12/6
 */
@Getter
public class SecurityValidatorRegistration {

    /**
     * 安全验证器
     */
    private SecurityValidator securityValidator;
    /**
     * 排序
     */
    private int order;


}
