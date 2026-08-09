package com.mujin.commons.web.utils;

import cn.hutool.core.util.StrUtil;
import com.mujin.commons.web.constants.RequestConstants;
import org.springframework.http.HttpHeaders;


/**
 * 请求 header 的工具类
 *
 * @author chenglin.wu
 * @date 2026/05/06
 */
public final class TokenUtil {
    private TokenUtil() {
    }

    /**
     * token前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";
    /**
     * token关键字
     */
    public static final String TOKEN_HEADER = HttpHeaders.AUTHORIZATION;


    /**
     * 通过请求对象获取token 兼容exchange
     *
     * @param request 请求对象
     * @return String
     * @date 2026/05/06
     */
    public static <T> String getTokenFromServlet(T request) {
        String token;
        token = RequestHeaderFactory.getSpecifyHeader(request, TOKEN_HEADER);
        if (StrUtil.isBlank(token)) {
            return null;
        }
        return token.replace(TOKEN_PREFIX, "");
    }

    /**
     * 获取请求来源
     *
     * @param request 请求对象
     * @return String
     * @date 2026/05/06
     */
    public static <T> String getRequestSource(T request) {
        String requestSource = RequestHeaderFactory.getSpecifyHeader(request, RequestConstants.REQUEST_SOURCE);
        if (StrUtil.isBlank(requestSource)) {
            requestSource = RequestHeaderFactory.getSpecifyHeader(request, RequestConstants.REQUEST_SOURCE_AGENT);
        }
        return StrUtil.isBlank(requestSource) ? null : requestSource;
    }


}
