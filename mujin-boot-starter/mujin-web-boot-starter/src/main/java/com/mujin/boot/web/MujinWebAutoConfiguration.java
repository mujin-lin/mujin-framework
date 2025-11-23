package com.mujin.boot.web;

import com.mujin.boot.web.constants.ConfigurationKeyConstants;
import com.mujin.commons.web.configuration.CommonsProperties;
import com.mujin.commons.web.configuration.CorsConfigProperties;
import com.mujin.commons.web.configuration.RequestInfoPrintConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 自动配置类
 *
 * @author chenglin.wu
 * @date 2025/11/23
 */
public class MujinWebAutoConfiguration {
    /**
     * 使用加密解密的 manager 配置指定
     *
     * @return CommonsProperties
     * @author chenglin.wu
     * @date 2025/11/23
     */
    @Bean
    @ConditionalOnMissingBean
    @ConfigurationProperties(ConfigurationKeyConstants.MUJIN_COMMONS_MANAGER_CONFIG_KEY)
    public CommonsProperties createCommonsProperties() {
        return new CommonsProperties();
    }

    /**
     * 跨域配置的类
     *
     * @return CommonsProperties
     * @author chenglin.wu
     * @date 2025/11/23
     */
    @Bean
    @ConditionalOnMissingBean
    @ConfigurationProperties(ConfigurationKeyConstants.MUJIN_COMMONS_CORS_CONFIG_KEY)
    public CorsConfigProperties createCorsConfigProperties() {
        return new CorsConfigProperties();
    }

    /**
     * 请求日志打印配置
     *
     * @return CommonsProperties
     * @author chenglin.wu
     * @date 2025/11/23
     */
    @Bean
    @ConditionalOnMissingBean
    @ConfigurationProperties(ConfigurationKeyConstants.MUJIN_COMMONS_REQUEST_LOG_CONFIG_KEY)
    public RequestInfoPrintConfig createRequestInfoPrintConfig() {
        return new RequestInfoPrintConfig();
    }
}
