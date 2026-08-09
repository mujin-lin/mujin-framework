package com.mujin.logging.db.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志主表实体（对应表 {@code ${table-prefix}operation_log}）
 * <p>
 * 表结构遵循 {@code docs/logging-design.md} 第 6.1 节。L5 阶段会基于本实体自动建表，
 * 当前阶段（L3）仅作为 MyBatis-Plus 写入载体。
 *
 * @author chenglin.wu
 * @date 2026/08/09
 */
@Data
@TableName("operation_log")
@SuppressWarnings("unused")
public class OperationLogEntity {

    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 链路追踪 ID
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
     * 执行结果：1=成功 0=失败
     */
    private Integer result;

    /**
     * 异常堆栈摘要
     */
    private String errorMessage;

    /**
     * 耗时（毫秒）
     */
    private Long costMs;

    /**
     * 是否慢方法
     */
    private Integer isSlow;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
