package com.mujin.logging.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 运行期操作日志上下文（L1 阶段先建空骨架）
 *
 * @author chenglin.wu
 * @date 2026/08/08
 */
@Data
public class OperationLogContext {

    /**
     * 链路 traceId
     */
    private String traceId;

    /**
     * SpEL 解析后的业务对象标识
     */
    private String bizId;

    /**
     * 类名
     */
    private String module;

    /**
     * 方法签名
     */
    private String method;

    /**
     * 操作描述
     */
    private String description;

    /**
     * 操作人
     */
    private String operator;

    /**
     * 请求 URI
     */
    private String requestUri;

    /**
     * HTTP 方法
     */
    private String httpMethod;

    /**
     * 客户端 IP
     */
    private String clientIp;

    /**
     * User-Agent
     */
    private String userAgent;

    /**
     * 请求头 JSON
     */
    private String requestHeaders;

    /**
     * 执行结果（1 成功 / 0 失败）
     */
    private int result;

    /**
     * 异常摘要
     */
    private String errorMessage;

    /**
     * 耗时（ms）
     */
    private long costMs;

    /**
     * 是否慢方法
     */
    private boolean slow;

    /**
     * 入参列表
     */
    private List<OperationLogParam> params = new ArrayList<>();

    /**
     * 出参
     */
    private OperationLogParam resultParam;
}
