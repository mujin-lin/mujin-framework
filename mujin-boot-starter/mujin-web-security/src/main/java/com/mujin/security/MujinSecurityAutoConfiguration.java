package com.mujin.security;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 自动配置类
 *
 * @author chenglin.wu
 * @date 2025/12/6
 */
public class MujinSecurityAutoConfiguration {

    @Bean
    public WebMvcConfigurer validatorConfigurer(){
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(new HandlerInterceptor() {
                    @Override
                    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
                        return HandlerInterceptor.super.preHandle(request, response, handler);
                    }
                }).addPathPatterns("/**").order(Integer.MIN_VALUE);
            }
        };
    }
}
