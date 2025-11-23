package com.mujin.commons.web.configuration;


import lombok.Data;

/**
 * 跨域配置的类
 *
 * @author chenglin.wu
 * @date 2023/12/6
 */
@Data
public class CorsConfigProperties {
    /**
     * 可能会跨域的路径
     */
    private String mappingPathPattern = "/**";
    /**
     * 允许跨域的匹配
     */
    private String[] allowedOriginPatterns = {"*"};
    /**
     * 允许跨域的header
     */
    private String[] allowedHeaders = {"requestsource", "RequestSource", "Referer", "sec-ch-ua", "sec-ch-ua-mobile", "Sec-Fetch-Dest",
            "Sec-Fetch-Mode", "Sec-Fetch-Site", "User-Agent", "Host", "Authorization", "Origin", "X-Requested-With",
            "ContentType", "Content-Type", "Accept", "Accept-Encoding", "Accept-Language", "custom-agent", "Custom-Agent"};
    /**
     * 允许跨域的请求方法
     */
    private String[] allowedMethods = {"GET", "POST", "DELETE", "PUT", "OPTIONS"};

    /**
     * 最大多少时间内不用发送option请求
     */
    private long maxAge = 1800000L;
    /**
     * 允许携带cookie等信息
     */
    private boolean allowCredentials = true;

    /**
     * 是否开启跨域
     */
    private boolean enableCors;
}
