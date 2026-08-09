package com.mujin.logging.persistence;

import com.mujin.logging.model.OperationLogContext;
import com.mujin.logging.model.OperationLogParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 操作日志上下文 → JSON 友好的 Map 序列化器
 * <p>
 * 抽取 {@link FileLogStorage} 中的转换逻辑，供 {@code FileLogStorage} 与
 * {@code KafkaLogStorage} 共享，保证两种后端输出结构一致。
 *
 * @author chenglin.wu
 * @date 2026/08/09
 */
public final class ContextJsonMapper {

    private ContextJsonMapper() {
    }

    /**
     * 将 {@link OperationLogContext} 转换为 {@link Map}，便于 JSON 序列化
     *
     * @param context 操作日志上下文
     * @return Map<String, Object> 序列化友好的结构
     */
    public static Map<String, Object> toMap(OperationLogContext context) {
        if (context == null) {
            return new HashMap<>();
        }
        Map<String, Object> map = new HashMap<>();
        map.put("traceId", context.getTraceId());
        map.put("bizId", context.getBizId());
        map.put("module", context.getModule());
        map.put("method", context.getMethod());
        map.put("description", context.getDescription());
        map.put("operator", context.getOperator());
        map.put("requestUri", context.getRequestUri());
        map.put("httpMethod", context.getHttpMethod());
        map.put("clientIp", context.getClientIp());
        map.put("userAgent", context.getUserAgent());
        map.put("requestHeaders", context.getRequestHeaders());
        map.put("result", context.getResult());
        map.put("errorMessage", context.getErrorMessage());
        map.put("costMs", context.getCostMs());
        map.put("slow", context.isSlow());
        map.put("createTime", System.currentTimeMillis());
        map.put("params", toList(context.getParams()));
        if (context.getResultParam() != null) {
            map.put("resultParam", toParamMap(context.getResultParam()));
        }
        return map;
    }

    /**
     * 入参列表转 List<Map>
     *
     * @param params 入参列表
     * @return List<Map<String, Object>> 序列化友好的列表
     */
    private static List<Map<String, Object>> toList(List<OperationLogParam> params) {
        if (params == null || params.isEmpty()) {
            return new ArrayList<>();
        }
        List<Map<String, Object>> list = new ArrayList<>(params.size());
        for (OperationLogParam param : params) {
            list.add(toParamMap(param));
        }
        return list;
    }

    /**
     * 单个 OperationLogParam 转 Map
     *
     * @param param 参数项
     * @return Map<String, Object> 序列化友好的结构
     */
    private static Map<String, Object> toParamMap(OperationLogParam param) {
        Map<String, Object> map = new HashMap<>();
        map.put("type", param.getParamType());
        map.put("index", param.getParamIndex());
        map.put("name", param.getParamName());
        map.put("value", param.getParamValue());
        return map;
    }
}
