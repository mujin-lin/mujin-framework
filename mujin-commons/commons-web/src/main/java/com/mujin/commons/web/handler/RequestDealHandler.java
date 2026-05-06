package com.mujin.commons.web.handler;


import org.springframework.util.MultiValueMap;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;

/**
 * 处理header的顶层接口
 *
 * @author chenglin.wu
 * @date 2026/05/06
 */
public interface RequestDealHandler {

    /**
     * 获取指定的header
     *
     * @param requestInstance the request
     * @param specifyHeader   the specifyHeader
     * @return String
     * @author chenglin.wu
     * @date 2026/05/06
     */
    <T> String getSpecifyHeader(RequestInstance<T> requestInstance, String specifyHeader);

    /**
     * 获取当前请求的所有header
     *
     * @param requestInstance the request
     * @return MultiValueMap<String, String>
     * @author chenglin.wu
     * @date 2026/05/06
     */
    <T> MultiValueMap<String, String> getAllHeaders(RequestInstance<T> requestInstance);


    /**
     * 获取IP地址
     *
     * @param tRequestInstance the tRequestInstance
     * @return String
     * @author chenglin.wu
     * @date 2026/05/06
     */
    <T> String getIp(RequestInstance<T> tRequestInstance);


    /**
     * 设置方法的访问权限
     *
     * @param method the method
     * @date: 2023/11/21
     */
    default void setMethodPublic(Method method) {
        if (Objects.isNull(method)) {
            return;
        }
        int modifiers = method.getModifiers();
        boolean isPublic = Modifier.isPublic(modifiers);
        if (!isPublic) {
            method.setAccessible(Boolean.TRUE);
        }
    }

}
