package com.mujin.security;


import com.mujin.security.constants.SecurityConfigurationConstants;
import com.mujin.security.filter.MjHttpServletRequestWrapperFilter;
import com.mujin.security.interceptor.ValidatorInterceptor;
import com.mujin.security.validator.SecurityValidator;
import com.mujin.security.validator.SecurityValidatorConfigurer;
import com.mujin.security.validator.SecurityValidatorRegistration;
import com.mujin.security.validator.SecurityValidatorRegistry;
import jakarta.annotation.Nonnull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * 自动配置类
 *
 * @author chenglin.wu
 * @date 2025/12/6
 */
public class MujinSecurityAutoConfiguration {

    /**
     * 请求包装类的 filter
     *
     * @return FilterRegistrationBean<MjHttpServletRequestWrapperFilter>
     * @date 2025/12/10
     */
    @Bean
    @ConditionalOnBooleanProperty(value = SecurityConfigurationConstants.ENABLE_REQUEST_WRAPPER)
    public FilterRegistrationBean<MjHttpServletRequestWrapperFilter> filterRegistrationBean() {
        FilterRegistrationBean<MjHttpServletRequestWrapperFilter> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(new MjHttpServletRequestWrapperFilter());
        filterRegistrationBean.addUrlPatterns("/*");
        filterRegistrationBean.setOrder(1);
        return filterRegistrationBean;
    }

    /**
     * 是否开启安全验证链
     *
     * @return WebMvcConfigurer
     * @date 2025/12/10
     */
    @Bean
    @ConditionalOnBooleanProperty(value = SecurityConfigurationConstants.ENABLE_SECURITY_VALIDATOR)
    @ConditionalOnBean(SecurityValidatorConfigurer.class)
    public WebMvcConfigurer validatorConfigurer(ObjectProvider<SecurityValidatorConfigurer> securityValidatorConfigurer) {
        SecurityValidatorRegistry registry = new SecurityValidatorRegistry();
        securityValidatorConfigurer.stream()
                .sorted()
                .forEach(securityValidator -> securityValidator.registryValidator(registry));
        // 获取当前容器中的所有validator
        List<SecurityValidator> validators = registry.getRegistrations().stream()
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(SecurityValidatorRegistration::getOrder))
                .map(SecurityValidatorRegistration::getSecurityValidator)
                .filter(Objects::nonNull)
                .toList();

        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(@Nonnull InterceptorRegistry registry) {
                registry.addInterceptor(new ValidatorInterceptor(validators)).addPathPatterns("/**").order(Integer.MIN_VALUE);
            }
        };
    }

}
