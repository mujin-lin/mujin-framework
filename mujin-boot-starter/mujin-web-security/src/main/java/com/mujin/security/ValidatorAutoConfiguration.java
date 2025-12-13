package com.mujin.security;

import cn.hutool.core.collection.CollectionUtil;
import com.mujin.security.constants.SecurityConfigurationConstants;
import com.mujin.security.interceptor.ValidatorInterceptor;
import com.mujin.security.properties.MjSecurityRequestProperties;
import com.mujin.security.validator.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * 验证器的自动配置类
 *
 * @author chenglin.wu
 * @date 2025/12/13
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnBean(SecurityValidatorConfigurer.class)
@ConditionalOnBooleanProperty(value = SecurityConfigurationConstants.ENABLE_SECURITY_VALIDATOR)
@AutoConfigureAfter({Environment.class, SecurityValidatorConfigurer.class})
public class ValidatorAutoConfiguration {
    /**
     * 是否开启安全验证链
     *
     * @return WebMvcConfigurer
     * @date 2025/12/10
     */
    @Bean
    public WebMvcConfigurer createWebMvcConfigurer(ObjectProvider<SecurityValidatorConfigurer> securityValidatorConfigurer,MjSecurityRequestProperties securityRequestProperties) {
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

        // 构造校验器链
        ConfigValidatorChain validatorChain = null;
        if (CollectionUtil.isNotEmpty(validators)) {
            validatorChain = this.buildValidatorChainRecursively(validators, 0);
        }
        return new ValidatorConfigurationChain(validatorChain,securityRequestProperties);
    }

    /**
     * 递归构建校验器链
     *
     * @param validators 校验器列表
     * @param index      当前处理的索引
     * @return 当前索引对应的校验器链节点
     */
    private ConfigValidatorChain buildValidatorChainRecursively(List<SecurityValidator> validators, int index) {
        // 递归终止条件：索引越界，无后续节点，返回null
        if (index >= validators.size()) {
            return null;
        }

        // 创建当前链节点
        ConfigValidatorChain currentChain = new ConfigValidatorChain();
        // 绑定当前索引对应的校验器（修正原代码仅第一个节点绑定的问题）
        currentChain.setValidator(validators.get(index));
        // 递归构建下一个节点，并设置为当前节点的后续链
        ConfigValidatorChain nextChain = buildValidatorChainRecursively(validators, index + 1);
        currentChain.setChain(nextChain);

        // 返回当前节点
        return currentChain;
    }

    /**
     * mvc config 用来包装验证器
     */
    private record ValidatorConfigurationChain(ConfigValidatorChain chain,MjSecurityRequestProperties securityRequestProperties) implements WebMvcConfigurer {

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(new ValidatorInterceptor(chain,securityRequestProperties)).addPathPatterns("/**").order(Integer.MIN_VALUE);
        }
    }


    /**
     * 配置的过滤器链，方便开放 add validator 方法和 addNext 方法
     */
    private static class ConfigValidatorChain extends SecurityValidatorChain {

        public ConfigValidatorChain() {
        }

        /**
         * 添加验证器
         *
         * @param validator 验证器
         * @date 2025/12/12
         */
        void setValidator(SecurityValidator validator) {
            super.addValidator(validator);
        }

        /**
         * 添加下一个节点
         *
         * @param chain 下一个节点
         * @date 2025/12/12
         */
        void setChain(SecurityValidatorChain chain) {
            super.addNext(chain);
        }
    }
}
