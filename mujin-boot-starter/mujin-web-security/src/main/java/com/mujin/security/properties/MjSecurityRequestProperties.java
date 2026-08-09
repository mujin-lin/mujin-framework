package com.mujin.security.properties;

import com.mujin.security.constants.SecurityConfigurationConstants;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 请求相关配置
 *
 * @author chenglin.wu
 * @date 2025/12/11
 */
@Data
@ConfigurationProperties(value = SecurityConfigurationConstants.MJ_SECURITY_REQUEST_KEY)
public class MjSecurityRequestProperties {
    /**
     * 请求 wrapper 是否封装为框架内可多次读取 body 的 wrapper
     */
    private boolean wrapperEnable;
    /**
     * 是否开启安全验证器
     */
    private boolean validatorEnable;
}
