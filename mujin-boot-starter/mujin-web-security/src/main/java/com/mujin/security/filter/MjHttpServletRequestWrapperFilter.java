package com.mujin.security.filter;

import com.mujin.commons.web.request.MjHttpRequestWrapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * 将请求对象替换成当前对应的能够重复读取的 filter
 *
 * @author chenglin.wu
 * @date 2025/12/10
 */
@Slf4j
public class MjHttpServletRequestWrapperFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest =(HttpServletRequest) request;
        log.debug("request uri:{}", httpServletRequest.getRequestURI());
        chain.doFilter(new MjHttpRequestWrapper(httpServletRequest), response);
    }
}
