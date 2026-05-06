package com.mujin.commons.web.handler;

import lombok.Data;

/**
 * request 包装类
 *
 * @author chenglin.wu
 */
@Data
@SuppressWarnings("unused")
public class RequestInstance<T> {
    private Class<?> requestClass;

    private T request;

    public RequestInstance(Class<?> requestClass) {
        this.requestClass = requestClass;
    }

    public RequestInstance(Class<?> requestClass, T request) {
        this.requestClass = requestClass;
        this.request = request;
    }

}
