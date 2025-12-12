package com.mujin.boot.web.constants;

/**
 * @author chenglin.wu
 * @date 2025/11/23
 */
public final class ConfigurationKeyConstants {

    private ConfigurationKeyConstants() {
    }

    /**
     * 使用加密解密的 manager key 配置
     */
    public static final String MUJIN_COMMONS_MANAGER_CONFIG_KEY = "mujin.web.config.commons";
    /**
     * 跨域配置
     */
    public static final String MUJIN_COMMONS_CORS_CONFIG_KEY = "mujin.web.config.cors";
    /**
     * 请求日志配置类
     */
    public static final String MUJIN_COMMONS_REQUEST_LOG_CONFIG_KEY = "mujin.web.config.request";
}
