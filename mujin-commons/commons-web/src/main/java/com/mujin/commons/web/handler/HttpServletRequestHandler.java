package com.mujin.commons.web.handler;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.mujin.commons.web.constants.ReflectConstants;
import com.mujin.commons.web.constants.RequestConstants;
import com.mujin.commons.web.utils.IpUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Objects;

/**
 * HttpServletRequest请求header对应的handler
 *
 * @author chenglin.wu
 * @date 2026/05/06
 */
@SuppressWarnings("unchecked")
public class HttpServletRequestHandler implements RequestDealHandler {

    private static final Logger log = LoggerFactory.getLogger(HttpServletRequestHandler.class);


    @Override
    public <T> String getSpecifyHeader(RequestInstance<T> requestInstance, String specifyHeader) {
        Class<?> requestClass = requestInstance.getRequestClass();
        T request = requestInstance.getRequest();
        String header = "";
        Method method;
        try {
            method = requestClass.getMethod(ReflectConstants.HTTP_SERVLET_REQUEST_GET_HEADER, String.class);
            // 设置方法访问权限
            setMethodPublic(method);
            Object invoke = method.invoke(request, specifyHeader);
            if (Objects.isNull(invoke)) {
                return header;
            }
            header = invoke.toString();
        } catch (Exception e) {
            log.error("get header has error: ", e);
        }
        return header;
    }

    @Override
    public <T> MultiValueMap<String, String> getAllHeaders(RequestInstance<T> requestInstance) {
        Class<?> requestClass = requestInstance.getRequestClass();
        T request = requestInstance.getRequest();
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        try {
            Method allHeaderNames = requestClass.getMethod(ReflectConstants.HTTP_SERVLET_REQUEST_GET_HEADER_NAMES);
            Method getHeader = requestClass.getMethod(ReflectConstants.HTTP_SERVLET_REQUEST_GET_HEADER, String.class);
            Enumeration<String> headerNames = (Enumeration<String>) allHeaderNames.invoke(request);
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                String headerVal = (String) getHeader.invoke(request, headerName);
                headers.put(headerName, CollectionUtil.newArrayList(headerVal));
            }
        } catch (Exception e) {
            log.error("get all header has error: ", e);
        }
        return headers;
    }

    @Override
    public <T> String getIp(RequestInstance<T> tRequestInstance) {
        return getIp((HttpServletRequest) tRequestInstance.getRequest());
    }


    /**
     * 获取httpServletRequest的ip地址
     *
     * @param request 请求对象
     * @return String
     * @date 2026/05/06
     */
    private String getIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String ip = request.getHeader(RequestConstants.X_FORWARDED_FOR);
        if (StrUtil.isBlank(ip) || RequestConstants.UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader(RequestConstants.PROXY_CLIENT_IP);
        }
        if (StrUtil.isBlank(ip) || RequestConstants.UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader(RequestConstants.X_FORWARDED_FOR_UPPER);
        }
        if (StrUtil.isBlank(ip) || RequestConstants.UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader(RequestConstants.WL_PROXY_CLIENT_IP);
        }
        if (StrUtil.isBlank(ip) || RequestConstants.UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader(RequestConstants.X_REAL_IP);
        }
        if (StrUtil.isBlank(ip) || RequestConstants.UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader(RequestConstants.HTTP_CLIENT_IP);
        }
        if (StrUtil.isBlank(ip) || RequestConstants.UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader(RequestConstants.HTTP_X_FORWARDED_FOR);
        }

        if (StrUtil.isBlank(ip) || RequestConstants.UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader(RequestConstants.REMOTE_ADDRESS);
        }
        if (StrUtil.isBlank(ip) || RequestConstants.UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        return IpUtils.transferIp(ip);
    }
}
