package com.mujin.commons.web.constants;

/**
 * ip 相关常量
 *
 * @author chenglin.wu
 * @date 2025/11/23
 */
public class RequestConstants {
    /**
     * 未知的
     */
    public static final String UNKNOWN = "unknown";
    /**
     * 本机内网IP
     */
    public static final String LOCALHOST = "127.0.0.1";
    /**
     * ipv6的本机IP
     */
    public static final String IPV6_LOCALHOST = "0:0:0:0:0:0:0:1";

    // --------------------------------------------------------------------
    //                              header
    // --------------------------------------------------------------------

    // region header相关常量
    /**
     * 转发
     */
    public static final String X_FORWARDED_FOR = "x-forwarded-for";
    /**
     * 某些部分大写的转发
     */
    public static final String X_FORWARDED_FOR_UPPER = "X-Forwarded-For";
    /**
     * wl 代理服务器
     */
    public static final String WL_PROXY_CLIENT_IP = "WL-Proxy-Client-IP";
    /**
     * 代理服务器代理的IP
     */
    public static final String PROXY_CLIENT_IP = "Proxy-Client-IP";
    /**
     * 真是IP
     */
    public static final String X_REAL_IP = "X-Real-IP";
    /**
     * HTTP_CLIENT_IP
     */
    public static final String HTTP_CLIENT_IP = "HTTP_CLIENT_IP";
    /**
     * HTTP_X_FORWARDED_FOR
     */
    public static final String HTTP_X_FORWARDED_FOR = "HTTP_X_FORWARDED_FOR";
    /**
     * remote addr key
     */
    public static final String REMOTE_ADDRESS = "REMOTE_ADDR_KEY";
    /**
     * 请求信息获取
     */
    public static final String USER_AGENT = "User-Agent";
    /**
     * 用户请求来源，取决用户登录的标记，此框架中使用 Custom-Agent 的header 或 RequestSource
     */
    public static final String REQUEST_SOURCE_AGENT = "Custom-Agent";
    /**
     * 请求来源
     */
    public static final String REQUEST_SOURCE = "RequestSource";

    // endregion

}
