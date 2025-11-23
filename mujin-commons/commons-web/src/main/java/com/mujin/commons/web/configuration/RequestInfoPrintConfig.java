package com.mujin.commons.web.configuration;


import lombok.Data;

/**
 * 请求来源打印config
 *
 * @author chenglin.wu
 * @date 2025/11/23
 */
@Data
public class RequestInfoPrintConfig {
    /**
     * 打印 uri
     */
    private boolean printUri;
    /**
     * 打印 请求ip
     */
    private boolean printRequestIp;
    /**
     * 打印操作系统
     */
    private boolean printRequestOs;
    /**
     * 打印请求来源标记区分用户登录哪个设备的标志
     */
    private boolean printRequestSource;
    /**
     * 打印请求的操作浏览器
     */
    private boolean printRequestBrowser;
    /**
     * 打印请求参数
     */
    private boolean printRequestParam;
    /**
     * 打印请求body
     */
    private boolean printRequestBody;

}
