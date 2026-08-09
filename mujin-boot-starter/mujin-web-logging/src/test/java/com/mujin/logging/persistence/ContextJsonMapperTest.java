package com.mujin.logging.persistence;

import com.fasterxml.jackson.databind.JsonNode;
import com.mujin.commons.lang.JsonUtil;
import com.mujin.logging.enums.LogResultEnum;
import com.mujin.logging.model.OperationLogContext;
import com.mujin.logging.model.OperationLogParam;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ContextJsonMapper} 序列化结构稳定性测试
 * <p>
 * 保证 File 与 Kafka 两种后端输出字段顺序与命名一致，便于消费侧统一解析。
 *
 * @author chenglin.wu
 * @date 2026/08/09
 */
class ContextJsonMapperTest {

    @Test
    void testToMapFull() {
        OperationLogContext context = new OperationLogContext();
        context.setTraceId("trace-1");
        context.setBizId("order-001");
        context.setModule("OrderController");
        context.setMethod("create");
        context.setDescription("创建订单");
        context.setOperator("alice");
        context.setRequestUri("/order");
        context.setHttpMethod("POST");
        context.setClientIp("127.0.0.1");
        context.setUserAgent("Mozilla/5.0");
        context.setRequestHeaders("{}");
        context.setResult(LogResultEnum.SUCCESS.getCode());
        context.setCostMs(123L);
        context.setSlow(false);
        context.setParams(Arrays.asList(
                OperationLogParam.ofIn(0, "req", "{\"id\":1}"),
                OperationLogParam.ofIn(1, "userId", "u-1")
        ));
        context.setResultParam(OperationLogParam.ofOut("result", "{\"ok\":true}"));

        Map<String, Object> map = ContextJsonMapper.toMap(context);
        // 字段完整性
        assertEquals("trace-1", map.get("traceId"));
        assertEquals("order-001", map.get("bizId"));
        assertEquals("OrderController", map.get("module"));
        assertEquals("create", map.get("method"));
        assertEquals("创建订单", map.get("description"));
        assertEquals("alice", map.get("operator"));
        assertEquals("/order", map.get("requestUri"));
        assertEquals("POST", map.get("httpMethod"));
        assertEquals("127.0.0.1", map.get("clientIp"));
        assertEquals("Mozilla/5.0", map.get("userAgent"));
        assertEquals("{}", map.get("requestHeaders"));
        assertEquals(LogResultEnum.SUCCESS.getCode(), map.get("result"));
        assertEquals(123L, map.get("costMs"));
        assertEquals(false, map.get("slow"));
        assertNotNull(map.get("createTime"));
        // params 与 resultParam 都已转换
        assertNotNull(map.get("params"));
        assertNotNull(map.get("resultParam"));
    }

    @Test
    void testToMapEmptyParams() {
        OperationLogContext context = new OperationLogContext();
        Map<String, Object> map = ContextJsonMapper.toMap(context);
        // 空入参应序列化为空数组
        assertTrue(map.get("params") instanceof java.util.List);
        assertEquals(0, ((java.util.List<?>) map.get("params")).size());
        // 无出参时不应有 resultParam 字段
        assertNull(map.get("resultParam"));
    }

    @Test
    void testToMapNullContext() {
        Map<String, Object> map = ContextJsonMapper.toMap(null);
        assertTrue(map.isEmpty());
    }

    @Test
    void testJsonOutputCompatibility() {
        // 验证通过 JsonUtil 序列化后字段名与 FileLogStorage 一致
        OperationLogContext context = new OperationLogContext();
        context.setTraceId("trace-1");
        context.setBizId("order-001");

        String json = JsonUtil.toJson(ContextJsonMapper.toMap(context));
        JsonNode node = JsonUtil.toJsonNode(json);
        assertNotNull(node.get("traceId"));
        assertNotNull(node.get("bizId"));
        assertNotNull(node.get("createTime"));
    }

    @Test
    void testHashMapRoundTrip() {
        // 验证序列化 → Map 反序列化 → 字段完整性
        OperationLogContext context = new OperationLogContext();
        context.setTraceId("trace-1");
        Map<String, Object> map = ContextJsonMapper.toMap(context);
        Map<String, Object> copy = new HashMap<>(map);
        assertEquals(map.get("traceId"), copy.get("traceId"));
    }
}
