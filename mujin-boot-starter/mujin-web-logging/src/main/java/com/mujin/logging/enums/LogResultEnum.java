package com.mujin.logging.enums;

import lombok.Getter;

/**
 * 操作日志执行结果
 *
 * @author chenglin.wu
 * @date 2026/08/08
 */
@Getter
public enum LogResultEnum {
    /**
     * 成功
     */
    SUCCESS(1, "成功"),
    /**
     * 失败
     */
    FAIL(0, "失败");

    /**
     * 数据库存储值
     */
    private final int code;

    /**
     * 中文描述
     */
    private final String desc;

    LogResultEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
