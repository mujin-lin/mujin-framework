package com.mujin.logging.kafka.auto;

import com.mujin.logging.configuration.LoggingProperties;
import com.mujin.logging.kafka.persistence.KafkaLogStorage;
import com.mujin.logging.persistence.LogStorage;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * 操作日志 Kafka 存储自动装配
 * <p>
 * 触发条件：
 * <ul>
 *     <li>{@code mujin.logging.enabled=true}（默认）</li>
 *     <li>{@code mujin.logging.storage-type=KAFKA}</li>
 *     <li>classpath 存在 {@link KafkaTemplate}（业务引入 spring-kafka）</li>
 *     <li>Spring 容器中存在 {@link KafkaTemplate} Bean（业务在 application.yml 配置了 spring.kafka.*）</li>
 * </ul>
 *
 * @author chenglin.wu
 * @date 2026/08/09
 */
@Configuration
@AutoConfigureAfter(name = "com.mujin.logging.auto.LoggingAutoConfiguration")
@EnableConfigurationProperties(LoggingProperties.class)
@ConditionalOnClass(KafkaTemplate.class)
@ConditionalOnBean(KafkaTemplate.class)
@ConditionalOnProperty(prefix = "mujin.logging", name = "storage-type", havingValue = "KAFKA")
public class LoggingKafkaAutoConfiguration {

    /**
     * Kafka 存储 Bean
     *
     * @param kafkaTemplate Kafka 模板（业务方配置 spring.kafka.* 后由 spring-kafka 自动装配）
     * @param properties     操作日志配置
     * @return LogStorage Kafka 实现
     */
    @Bean
    @ConditionalOnMissingBean(LogStorage.class)
    @SuppressWarnings("unchecked")
    public LogStorage kafkaLogStorage(KafkaTemplate<String, String> kafkaTemplate,
                                      LoggingProperties properties) {
        return new KafkaLogStorage(kafkaTemplate, properties);
    }
}
