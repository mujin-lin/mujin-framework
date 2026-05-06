package com.mujin.commons.web.utils;


import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ClassUtil;
import com.mujin.commons.web.constants.ReflectConstants;
import com.mujin.commons.web.handler.HttpServletRequestHandler;
import com.mujin.commons.web.handler.RequestDealHandler;
import com.mujin.commons.web.handler.RequestInstance;
import com.mujin.commons.web.handler.ServerRequestHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.Advised;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 获取请求header的工厂
 *
 * @author chenglin.wu
 * @date 2026/05/06
 */
@Slf4j
@SuppressWarnings("ALL")
public class RequestHeaderFactory {

    /**
     * handler map
     */
    private static final Map<String, RequestDealHandler> HANDLER_MAP = new HashMap<>();

    static {
        HANDLER_MAP.put(ReflectConstants.HTTP_SERVLET_REQUEST_PACKAGE, new HttpServletRequestHandler());

        HANDLER_MAP.put(ReflectConstants.REACTIVE_REQUEST_PACKAGE, new ServerRequestHandler());
    }

    /**
     * 注册处理对象
     *
     * @param dealClassName the dealClassName
     * @param handler       the handler
     * @return void
     * @date 2026/05/06
     */
    public static void registerHandler(String dealClassName, RequestDealHandler handler) {
        HANDLER_MAP.put(dealClassName, handler);
    }

    /**
     * 获取指定header
     *
     * @param request       the request
     * @param specifyHeader the specifyHeader
     * @return String
     * @date 2026/05/06
     */
    public static <T> String getSpecifyHeader(T request, String specifyHeader) {
        Class<?> aClass = request.getClass();
        RequestInstance<T> tRequestInstance = new RequestInstance<>(aClass, request);
        RequestDealHandler requestDealHandler = getHandler(aClass, request);
        return requestDealHandler.getSpecifyHeader(tRequestInstance, specifyHeader);
    }

    /**
     * 获取所有的header
     *
     * @param request the request
     * @return MultiValueMap<String, String>
     * @date 2026/05/06
     */
    public static <T> MultiValueMap<String, String> getAllHeaders(T request) {
        Class<?> aClass = request.getClass();
        RequestDealHandler requestDealHandler = getHandler(aClass, request);
        RequestInstance<T> tRequestInstance = new RequestInstance<>(aClass, request);

        return requestDealHandler.getAllHeaders(tRequestInstance);
    }

    public static <T> String getIp(T request) {
        Class<?> aClass = request.getClass();
        RequestDealHandler requestDealHandler = getHandler(aClass, request);
        RequestInstance<T> tRequestInstance = new RequestInstance<>(aClass, request);
        return requestDealHandler.getIp(tRequestInstance);
    }

    /**
     * 获取handler
     *
     * @param aClass the aClass
     * @return RequestDealHandler
     * @date 2026/05/06
     */
    private static <T> RequestDealHandler getHandler(Class<?> aClass, T request) {
        String className = aClass.getName();
        String packageCanonicalName = ClassUtil.getPackage(aClass);
        StringBuilder packageNames = new StringBuilder();
        packageNames.append(packageCanonicalName);
        try {
            conllectSuperClass(aClass, packageNames);
            conllectInterfaces(aClass, null, packageNames);
        } catch (Exception e) {
            log.error("had error:", e);
        }
        boolean contains = packageNames.toString().contains(ReflectConstants.HTTP_SERVLET_REQUEST_PACKAGE);
        if (contains) {
            return HANDLER_MAP.get(ReflectConstants.HTTP_SERVLET_REQUEST_PACKAGE);
        }
        return HANDLER_MAP.get(ReflectConstants.REACTIVE_REQUEST_PACKAGE);
    }

    /**
     * 获取顶层父类的信息
     *
     * @param aClass       the aClass
     * @param packageNames the packageNames
     * @return void
     * @date 2026/05/06
     */
    private static void conllectSuperClass(Class<?> aClass, StringBuilder packageNames) {
        Class<?> superclass = aClass.getSuperclass();
        if (Objects.isNull(superclass)) {
            return;
        }
        Package aPackage = superclass.getPackage();
        if (Objects.nonNull(aPackage)) {
            packageNames.append(aPackage.getName());
        }
        conllectSuperClass(superclass, packageNames);
    }

    /**
     * 获取顶层接口信息
     *
     * @param aClass       the aClass
     * @param interfaces   the interfaces
     * @param packageNames the packageNames
     * @date 2026/05/06
     */
    private static void conllectInterfaces(Class<?> aClass, Class<?>[] interfaces, StringBuilder packageNames) {
        Class<?>[] interfacesNew = aClass.getInterfaces();
        if (ArrayUtil.isEmpty(interfacesNew)) {
            return;
        }
        for (Class<?> aClass1 : interfacesNew) {
            Package aPackage = aClass1.getPackage();
            if (Objects.nonNull(aPackage)) {
                packageNames.append(aPackage.getName());
            }
            Class<?>[] interfaces1 = aClass.getInterfaces();
            conllectInterfaces(aClass1, interfaces1, packageNames);
        }
    }

    /**
     * 获取对象名
     *
     * @param target the target
     * @return String
     * @date 2026/05/06
     */
    private String getTargetName(final Object target) {
        if (target == null) {
            return "";
        }
        if (targetClassIsProxied(target)) {
            Advised advised = (Advised) target;
            try {
                return advised.getTargetSource().getTarget().getClass().getCanonicalName();
            } catch (Exception e) {
                return "";
            }
        }
        return target.getClass().getCanonicalName();
    }

    /**
     * 当前对象是否被代理
     *
     * @param target the target
     * @return boolean
     * @date 2026/05/06
     */
    private boolean targetClassIsProxied(final Object target) {
        return target.getClass().getCanonicalName().contains("$Proxy");
    }

}
