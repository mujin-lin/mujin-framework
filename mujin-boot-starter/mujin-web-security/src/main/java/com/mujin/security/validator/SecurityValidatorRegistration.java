package com.mujin.security.validator;

/**
 * 校验器注册包装类
 *
 * @author chenglin.wu
 * @date 2025/12/6
 */

import lombok.Getter;

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

    /**
     * 创建时间
     */
    private long timestamp;


    public SecurityValidatorRegistration(SecurityValidator securityValidator) {
        this.securityValidator = securityValidator;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * 执行顺序
     *
     * @param order 执行顺序
     * @return SecurityValidatorRegistration
     * @author chenglin.wu
     * @date 2025/12/07
     */
    public SecurityValidatorRegistration order(int order) {
        this.order = order;
        return this;
    }
}
