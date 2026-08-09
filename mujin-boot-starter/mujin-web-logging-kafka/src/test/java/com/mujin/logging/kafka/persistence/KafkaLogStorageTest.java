package com.mujin.logging.kafka.persistence;

import com.mujin.logging.configuration.LoggingProperties;
import com.mujin.logging.model.OperationLogContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * {@link KafkaLogStorage} 发送行为回归测试
 *
 * @author chenglin.wu
 * @date 2026/08/09
 */
@SuppressWarnings("unchecked")
class KafkaLogStorageTest {

    private KafkaTemplate<String, String> kafkaTemplate;
    private KafkaLogStorage storage;

    @BeforeEach
    void setUp() {
        kafkaTemplate = mock(KafkaTemplate.class);
        LoggingProperties properties = new LoggingProperties();
        properties.getKafka().setTopic("test-operation-log");
        storage = new KafkaLogStorage(kafkaTemplate, properties);
    }

    @Test
    void testSendWithBizIdAsKey() {
        OperationLogContext context = new OperationLogContext();
        context.setTraceId("trace-1");
        context.setBizId("order-001");
        context.setDescription("创建订单");

        storage.save(context);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate, times(1)).send(eq("test-operation-log"), keyCaptor.capture(), valueCaptor.capture());
        // key 应使用 bizId
        assertEquals("order-001", keyCaptor.getValue());
        // value 是 JSON 字符串，包含 description
        assertNotNull(valueCaptor.getValue());
        assertTrue(valueCaptor.getValue().contains("\"description\""));
        assertTrue(valueCaptor.getValue().contains("\"traceId\":\"trace-1\""));
    }

    @Test
    void testSendWithTraceIdAsKeyWhenBizIdMissing() {
        OperationLogContext context = new OperationLogContext();
        context.setTraceId("trace-2");

        storage.save(context);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate, times(1)).send(any(String.class), keyCaptor.capture(), any(String.class));
        // bizId 为空时 key 回退为 traceId
        assertEquals("trace-2", keyCaptor.getValue());
    }

    @Test
    void testSendSwallowException() {
        // 模拟 send 抛异常，不应污染业务
        org.mockito.Mockito.doThrow(new RuntimeException("kafka down"))
                .when(kafkaTemplate).send(any(String.class), any(String.class), any(String.class));

        OperationLogContext context = new OperationLogContext();
        context.setTraceId("trace-3");

        // 不应抛异常
        storage.save(context);
    }

    @Test
    void testSaveNullContext() {
        storage.save(null);
        // null context 时不发送
        verify(kafkaTemplate, times(0)).send(any(String.class), any(String.class), any(String.class));
    }
}
