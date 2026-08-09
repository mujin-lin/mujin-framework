package com.mujin.logging.configuration;

import com.mujin.logging.enums.LogStorageType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.Collections;
import java.util.List;

/**
 * 操作日志配置（prefix=mujin.logging）
 *
 * @author chenglin.wu
 * @date 2026/08/08
 */
@Data
@ConfigurationProperties(prefix = "mujin.logging")
public class LoggingProperties {

    /**
     * 总开关，默认开启
     */
    private boolean enabled = true;

    /**
     * 存储后端类型
     */
    private LogStorageType storageType = LogStorageType.DB;

    /**
     * 是否异步写入（推荐 true）
     */
    private boolean async = true;

    /**
     * 异步线程池核心大小
     */
    private int threadPoolSize = 4;

    /**
     * 异步线程池队列容量（满则降级同步）
     */
    private int queueCapacity = 1024;

    /**
     * 全局慢方法阈值（ms）
     */
    private long slowThreshold = 3000L;

    /**
     * AOP 扫描包；留空表示不限制
     */
    private List<String> includePackages = Collections.emptyList();

    /**
     * 是否记录请求头
     */
    private boolean captureHeader = true;

    /**
     * 数据库存储配置
     */
    @NestedConfigurationProperty
    private Db db = new Db();

    /**
     * 文件存储配置
     */
    @NestedConfigurationProperty
    private File file = new File();

    /**
     * Kafka 存储配置
     */
    @NestedConfigurationProperty
    private Kafka kafka = new Kafka();

    /**
     * 数据库存储子配置
     */
    @Data
    public static class Db {
        /**
         * 表名前缀，最终表名 = prefix + operation_log
         */
        private String tablePrefix = "mujin_";

        /**
         * 是否启动时自动建表（仅 MySQL）
         */
        private boolean autoCreateTable = true;

        /**
         * 独立数据源 Bean 名称；留空=复用业务数据源
         */
        private String datasourceBeanName = "";
    }

    /**
     * 文件存储子配置
     */
    @Data
    public static class File {
        /**
         * 日志文件目录
         */
        private String basePath = "./logs/operation";

        /**
         * 日志保留天数
         */
        private int maxHistory = 30;

        /**
         * 单文件最大大小
         */
        private String maxFileSize = "50MB";
    }

    /**
     * Kafka 存储子配置
     */
    @Data
    public static class Kafka {
        /**
         * Kafka topic
         */
        private String topic = "mujin-operation-log";

        /**
         * Kafka 集群地址
         */
        private String bootstrapServers = "localhost:9092";
    }
}
