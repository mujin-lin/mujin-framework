package com.mujin.logging.collector;

import com.mujin.logging.annotations.OperationLog;
import com.mujin.logging.model.OperationLogContext;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Web 上下文采集器：traceId / IP / UA / URI / HTTP method / 请求头
 * <p>
 * 非 Web 调用栈（定时任务 / 内部调用）下静默跳过，不抛异常。
 * traceId 优先从请求头 {@code X-Trace-Id} 读，缺失则生成 UUID 兜底。
 *
 * @author chenglin.wu
 * @date 2026/08/08
 */
public class WebContextCollector implements OperationLogCollector {

    private static final Logger LOG = LoggerFactory.getLogger(WebContextCollector.class);

    /**
     * traceId 请求头名称
     */
    public static final String HEADER_TRACE_ID = "X-Trace-Id";

    /**
     * 代理头（按顺序读取，遇到非空即用）
     */
    private static final String[] IP_HEADERS = {
            "X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP"
    };

    private final boolean captureHeader;

    public WebContextCollector(boolean captureHeader) {
        this.captureHeader = captureHeader;
    }

    @Override
    public int order() {
        // Web 上下文独立，与 SpEL 无依赖
        return 0;
    }

    @Override
    public void collect(OperationLogContext context, ProceedingJoinPoint joinPoint, OperationLog annotation) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            // 非 Web 场景（如 @Scheduled 触发），写入空 traceId 兜底
            context.setTraceId(UUID.randomUUID().toString().replace("-", ""));
            return;
        }

        try {
            HttpServletRequest request = attributes.getRequest();
            context.setTraceId(resolveTraceId(request));
            context.setRequestUri(request.getRequestURI());
            context.setHttpMethod(request.getMethod());
            context.setClientIp(resolveClientIp(request));
            context.setUserAgent(safeHeader(request, "User-Agent"));
            if (captureHeader) {
                context.setRequestHeaders(snapshotHeaders(request));
            }
        } catch (Exception e) {
            // 任何异常都不污染主流程，仅打 warn
            LOG.warn("[OPERATION-LOG] Web 上下文采集失败：{}", e.getMessage());
        }
    }

    /**
     * 解析 traceId：优先请求头，其次生成 UUID
     *
     * @param request 当前请求
     * @return String traceId
     */
    private String resolveTraceId(HttpServletRequest request) {
        String traceId = safeHeader(request, HEADER_TRACE_ID);
        if (traceId != null && !traceId.isEmpty()) {
            return traceId;
        }
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 按代理头顺序解析客户端 IP
     *
     * @param request 当前请求
     * @return String IP；解析不到返回 unknown
     */
    private String resolveClientIp(HttpServletRequest request) {
        for (String header : IP_HEADERS) {
            String ip = safeHeader(request, header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For 可能是 "client, proxy1, proxy2"，取第一个
                int comma = ip.indexOf(',');
                return comma > 0 ? ip.substring(0, comma).trim() : ip.trim();
            }
        }
        return request.getRemoteAddr();
    }

    /**
     * 读取单个请求头，统一处理 null
     *
     * @param request 当前请求
     * @param name    header 名
     * @return String 值；缺失返回 null
     */
    private String safeHeader(HttpServletRequest request, String name) {
        String value = request.getHeader(name);
        return (value == null || value.isEmpty()) ? null : value;
    }

    /**
     * 快照所有请求头（用于日志展示）
     *
     * @param request 当前请求
     * @return String 形如 {@code {"k1":"v1","k2":"v2"}}
     */
    private String snapshotHeaders(HttpServletRequest request) {
        Map<String, String> map = new LinkedHashMap<>();
        Enumeration<String> names = request.getHeaderNames();
        while (names != null && names.hasMoreElements()) {
            String name = names.nextElement();
            // 跳过敏感头
            if (HttpHeaders.AUTHORIZATION.equalsIgnoreCase(name) || HttpHeaders.COOKIE.equalsIgnoreCase(name)) {
                continue;
            }
            map.put(name, request.getHeader(name));
        }
        return map.toString();
    }
}
