package com.mujin.logging.kafka.persistence;

import com.mujin.commons.lang.JsonUtil;
import com.mujin.logging.configuration.LoggingProperties;
import com.mujin.logging.model.OperationLogContext;
import com.mujin.logging.persistence.ContextJsonMapper;
import com.mujin.logging.persistence.LogStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * Kafka 存储策略：将 {@link OperationLogContext} 序列化为 JSON 消息发送到指定 topic
 * <p>
 * 实现要点：
 * <ul>
 *     <li>topic 由 {@link LoggingProperties.Kafka#getTopic()} 决定，业务可在 application.yml 中覆盖</li>
 *     <li>消息 key：{@code bizId} 优先，回退为 {@code traceId}，
 *         保证同一业务对象的日志落在同一 Kafka 分区</li>
 *     <li>value：复用 {@link ContextJsonMapper#toMap} 序列化结构，与文件模式输出对齐</li>
 *     <li>发送异常 try-catch + warn，不污染业务主流程</li>
 * </ul>
 *
 * @author chenglin.wu
 * @date 2026/08/09
 */
public class KafkaLogStorage implements LogStorage {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaLogStorage.class);

    /**
     * Kafka 模板（业务方注入：spring.kafka.bootstrap-servers）
     */
    private final KafkaTemplate<String, String> kafkaTemplate;

    /**
     * Kafka topic
     */
    private final String topic;

    public KafkaLogStorage(KafkaTemplate<String, String> kafkaTemplate, LoggingProperties properties) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = properties.getKafka().getTopic();
    }

    @Override
    public void save(OperationLogContext context) {
        if (context == null) {
            return;
        }
        try {
            String key = resolveKey(context);
            String value = JsonUtil.toJson(ContextJsonMapper.toMap(context));
            kafkaTemplate.send(topic, key, value);
        } catch (Exception e) {
            LOG.warn("[OPERATION-LOG] Kafka 发送失败：topic={}, err={}", topic, e.getMessage(), e);
        }
    }

    /**
     * 解析消息 key：优先 bizId，回退 traceId，确保同业务对象消息有序
     *
     * @param context 操作日志上下文
     * @return String 消息 key
     */
    private String resolveKey(OperationLogContext context) {
        if (context.getBizId() != null && !context.getBizId().isEmpty()) {
            return context.getBizId();
        }
        return context.getTraceId();
    }
}
