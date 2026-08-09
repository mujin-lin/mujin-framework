package com.mujin.logging.enums;

import lombok.Getter;

/**
 * 日志存储后端类型
 *
 * @author chenglin.wu
 * @date 2026/08/08
 */
@Getter
public enum LogStorageType {
    /**
     * 关系型数据库（默认）
     */
    DB("database"),
    /**
     * 日志文件（Logback JSON 行）
     */
    FILE("file"),
    /**
     * Kafka 消息队列
     */
    KAFKA("kafka");

    /**
     * 类型描述
     */
    private final String desc;

    LogStorageType(String desc) {
        this.desc = desc;
    }
}
