package com.mujin.logging.persistence;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.JsonEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.OutputStreamAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;
import com.mujin.commons.lang.JsonUtil;
import com.mujin.logging.configuration.LoggingProperties;
import com.mujin.logging.model.OperationLogContext;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * 文件存储策略：将 {@link OperationLogContext} 序列化为 JSON 行写入滚动日志文件
 * <p>
 * 实现要点：
 * <ul>
 *     <li>在初始化时手动构造 Logback {@link RollingFileAppender}，不依赖外部 logback.xml</li>
 *     <li>每条日志独立一行 JSON，便于后续 grep / jq 分析</li>
 *     <li>滚动策略：{@link SizeAndTimeBasedRollingPolicy} 按天+大小归档，保留 {@code maxHistory} 天</li>
 *     <li>文件名：{@code ${basePath}/operation-yyyy-MM-dd.%i.log}</li>
 * </ul>
 * 该类仅在 {@code mujin.logging.storage-type=FILE} 时由 {@code LoggingAutoConfiguration} 装配。
 *
 * @author chenglin.wu
 * @date 2026/08/09
 */
public class FileLogStorage implements LogStorage {

    /**
     * 单文件最大大小（默认 50MB，由 LoggingProperties.file.maxFileSize 覆盖）
     */
    private static final String DEFAULT_MAX_FILE_SIZE = "50MB";

    /**
     * 内部 Logback appender 实例
     */
    private final OutputStreamAppender<ILoggingEvent> appender;

    /**
     * 构造一个基于 Logback RollingFileAppender 的文件存储
     *
     * @param properties 操作日志配置（读取 file.* 子配置）
     */
    public FileLogStorage(LoggingProperties properties) {
        this.appender = buildAppender(properties);
    }

    /**
     * 构建 Logback RollingFileAppender
     *
     * @param properties 配置
     * @return OutputStreamAppender 已 start 的 appender
     */
    private OutputStreamAppender<ILoggingEvent> buildAppender(LoggingProperties properties) {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        String basePath = properties.getFile().getBasePath();
        ensureDirectory(basePath);

        RollingFileAppender<ILoggingEvent> rolling = new RollingFileAppender<>();
        rolling.setContext(context);
        rolling.setName("OPERATION_LOG_FILE");
        rolling.setFile(basePath + File.separator + "operation.log");
        rolling.setAppend(true);

        // 时间+大小滚动策略：每天一个文件，单文件超 maxFileSize 自动触发滚动
        SizeAndTimeBasedRollingPolicy<ILoggingEvent> policy = new SizeAndTimeBasedRollingPolicy<>();
        policy.setContext(context);
        policy.setParent(rolling);
        policy.setFileNamePattern(basePath + File.separator + "operation-%d{yyyy-MM-dd}.%i.log");
        policy.setMaxHistory(Math.max(1, properties.getFile().getMaxHistory()));
        String maxSize = properties.getFile().getMaxFileSize();
        if (maxSize == null || maxSize.isEmpty()) {
            maxSize = DEFAULT_MAX_FILE_SIZE;
        }
        policy.setMaxFileSize(FileSize.valueOf(maxSize));
        policy.start();

        rolling.setRollingPolicy(policy);

        // JSON 行编码器（Logback 自带）
        JsonEncoder encoder = new JsonEncoder();
        encoder.setContext(context);
        encoder.start();
        rolling.setEncoder(encoder);

        rolling.start();
        return rolling;
    }

    /**
     * 确保日志目录存在
     *
     * @param basePath 基础路径
     */
    private void ensureDirectory(String basePath) {
        File dir = new File(basePath);
        if (!dir.exists() && !dir.mkdirs()) {
            // 不抛异常：后续 RollingFileAppender 启动会再次尝试创建并给出错误日志
        }
    }

    @Override
    public void save(OperationLogContext context) {
        if (context == null) {
            return;
        }
        // 序列化为单行 JSON（复用 ContextJsonMapper，与 KafkaLogStorage 输出结构一致）
        String json = JsonUtil.toJson(ContextJsonMapper.toMap(context));
        // 通过 SLF4J 写入文件 appender，避免直接依赖 appender.doAppend()
        Logger logger = (Logger) LoggerFactory.getLogger("OPERATION_LOG");
        if (!logger.isAttached(appender)) {
            logger.addAppender(appender);
        }
        // 关闭 additivity 防止 JSON 行被 root logger 重复输出
        logger.setAdditive(false);
        // 仅 INFO 级别，使用 JsonEncoder 输出
        logger.info(json);
    }
}
