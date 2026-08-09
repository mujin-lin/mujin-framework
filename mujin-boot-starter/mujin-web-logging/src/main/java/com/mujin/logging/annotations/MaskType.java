package com.mujin.logging.annotations;

/**
 * 敏感字段脱敏策略
 *
 * @author chenglin.wu
 * @date 2026/08/08
 */
public enum MaskType {
    /**
     * 保留头部 N 位，其余替换为 *
     */
    KEEP_HEAD,
    /**
     * 保留尾部 N 位，其余替换为 *
     */
    KEEP_TAIL,
    /**
     * 仅保留头尾，中间替换为 *
     */
    MIDDLE,
    /**
     * 全部替换为 *
     */
    ALL
}