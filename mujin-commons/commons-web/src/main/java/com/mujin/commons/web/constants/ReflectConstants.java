package com.mujin.commons.web.constants;


/**
 * 反射相关常量
 *
 * @author chenglin.wu
 * @date 2025/12/27 13:18
 */
public final class ReflectConstants {

    /**
     * 私有化常量类的构造器
     */
    private ReflectConstants() {
    }

    /**
     * set方法的前缀
     */
    public static final String SET_PREFIX = "set";

    /**
     * get方法的前缀
     */
    public static final String GET_PREFIX = "get";

    /**
     * boolean方法获取值的前缀
     */
    public static final String IS_PREFIX = "is";
    /**
     * String
     */
    public static final String STRING_SIMPLE_NAME = "String";
    /**
     * BaseEntity的方法 getParams名称
     */
    public static final String GET_PARAMS = "getParams";
    // ---------------------------------------------------
    //               web 请求方式的参数和全类名
    // ---------------------------------------------------

    /**
     * tomcat 中HttpServletRequest中获取header的方法,<p/>
     * 详情{@link javax.servlet.http.HttpServletRequest#getHeader(String)}
     */
    public static final String HTTP_SERVLET_REQUEST_GET_HEADER = "getHeader";
    /**
     * tomcat 中HttpServletRequest中获取header的方法,<p/>
     * 详情{@link javax.servlet.http.HttpServletRequest#getHeaderNames()}
     */
    public static final String HTTP_SERVLET_REQUEST_GET_HEADER_NAMES = "getHeaderNames";
    /**
     * tomcat 中ServerHttpRequest中获取header的方法,<p/>
     * 详情{@link org.springframework.http.server.ServerHttpRequest#getHeaders()}
     */
    public static final String SERVER_REQUEST_GET_HEADERS = "getHeaders";

    /**
     * HttpSercletRequest 类的包名
     */
    public static final String HTTP_SERVLET_REQUEST_PACKAGE = "javax.servlet.http";
    /**
     * ServerHttpRequest 包名
     */
    public static final String REACTIVE_REQUEST_PACKAGE = "org.springframework.http.server";

    // ---------------------------------------------------
    //    扩展字段分页查询的区间判断常量
    // ---------------------------------------------------
    /**
     * 全闭区间
     */
    public static final String CLOSE_ALL = "closeAll";
    /**
     * 左开右闭
     */
    public static final String OPEN_LEFT_AND_CLOSE_RIGHT = "openLeftAndCloseRight";
    /**
     * 左闭右开
     */
    public static final String CLOSE_LEFT_AND_OPEN_RIGHT = "closeLeftAndOpenRight";
    /**
     * 全开
     */
    public static final String OPEN_ALL = "openAll";

}
