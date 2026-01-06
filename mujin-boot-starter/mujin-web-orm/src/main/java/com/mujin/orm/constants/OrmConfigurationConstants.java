package com.mujin.orm.constants;

/**
 * 持久化层配置信息
 *
 * @author chenglin.wu
 * @date 2025/12/28
 */
public final class OrmConfigurationConstants {

    private OrmConfigurationConstants() {
    }

    /**
     * 持久化层配置
     */
    public static final String MJ_ORM_CONFIG_KEY = "mujin.web.config.orm";
    /**
     * 是否开启自动注入扫描
     */
    public static final String MJ_ORM_ENABLE_AUTO_FILL_KEY = "mujin.web.config.orm.enable-auto-fill";
}
