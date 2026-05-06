package com.mujin.commons.web.handler;


import com.mujin.commons.web.constants.ReflectConstants;
import com.mujin.commons.web.utils.IpUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * 响应式请求对象获取heander数据
 *
 * @author chenglin.wu
 * @date 2026/05/06
 */
@SuppressWarnings("all")
@Slf4j
public class ServerRequestHandler implements RequestDealHandler {

    @Override
    public <T> String getSpecifyHeader(RequestInstance<T> requestInstance, String specifyHeader) {
        T request = requestInstance.getRequest();
        Class<?> requestClass = requestInstance.getRequestClass();
        String header = "";
        try {

            Method method = requestClass.getMethod(ReflectConstants.SERVER_REQUEST_GET_HEADERS);
            if (Objects.isNull(method)) {
                return header;
            }
            // 设置方法访问权限
            setMethodPublic(method);
            MultiValueMap<String, String> httpHeaders = (MultiValueMap<String, String>) method.invoke(request);
            header = httpHeaders.getFirst(specifyHeader);
        } catch (Exception e) {
            log.error("get header has error: ", e);
        }
        return header;
    }

    @Override
    public <T> MultiValueMap<String, String> getAllHeaders(RequestInstance<T> requestInstance) {
        T request = requestInstance.getRequest();
        Class<?> requestClass = requestInstance.getRequestClass();
        MultiValueMap<String, String> httpHeaders = new LinkedMultiValueMap<>();
        try {
            Method method = requestClass.getMethod(ReflectConstants.SERVER_REQUEST_GET_HEADERS);
            if (Objects.isNull(method)) {
                return httpHeaders;
            }
            // 设置方法访问权限
            setMethodPublic(method);
            httpHeaders = (MultiValueMap<String, String>) method.invoke(request);
        } catch (Exception e) {
            log.error("get header has error: ", e);
        }
        return httpHeaders;
    }

    @Override
    public <T> String getIp(RequestInstance<T> tRequestInstance) {
        T request = tRequestInstance.getRequest();
        ServerHttpRequest serverHttpRequest = (ServerHttpRequest) request;
        String ip = serverHttpRequest.getRemoteAddress().getAddress().getHostAddress();
        return IpUtils.transferIp(ip);

    }
}
