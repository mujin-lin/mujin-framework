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
     * 是否启用 request 包装类 true 启用
     */
    public static final String ENABLE_REQUEST_WRAPPER = "mujin.web.security.request.wrapper.enable";
    /**
     * 是否启用安全验证
     */
    public static final String ENABLE_SECURITY_VALIDATOR = "mujin.web.security.request.security.enable";
}

