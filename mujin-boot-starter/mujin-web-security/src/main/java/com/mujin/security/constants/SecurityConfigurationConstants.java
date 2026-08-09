package com.mujin.security.constants;

/**
 * 安全相关的配置类
 *
 * @author chenglin.wu
 * @date 2025/12/10
 */
public final class SecurityConfigurationConstants {

    private SecurityConfigurationConstants() {
    }

    /**
     * 配置信息
     */
    public static final String MJ_SECURITY_REQUEST_KEY = "mujin.web.config.request.security";

    /**
     * 是否启用 request 包装类 true 启用
     */
    public static final String ENABLE_REQUEST_WRAPPER = "mujin.web.config.request.security.wrapper-enable";
    /**
     * 是否启用安全验证
     */
    public static final String ENABLE_SECURITY_VALIDATOR = "mujin.web.config.request.security.validator-enable";
}

