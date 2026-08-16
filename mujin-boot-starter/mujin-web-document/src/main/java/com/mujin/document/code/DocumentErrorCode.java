package com.mujin.document.code;

import com.mujin.commons.lang.code.ErrorCodeDefinition;

/**
 * 文档模块错误码定义
 * <p>
 * 错误码段位：6001-6099。
 *
 * @author chenglin.wu
 * @date 2026/08/16
 */
public enum DocumentErrorCode implements ErrorCodeDefinition {
    /**
     * 导出失败（PDF/JSON/YAML 通用）
     */
    EXPORT_FAILED(6001),
    /**
     * OpenAPI 规范未找到
     */
    SPEC_NOT_FOUND(6002),
    /**
     * PDF 渲染失败
     */
    PDF_RENDER_FAILED(6003),
    /**
     * 序列化失败
     */
    SERIALIZE_FAILED(6004),
    /**
     * 参数无效
     */
    INVALID_PARAM(6005);

    /**
     * 错误码
     */
    private final int errorCode;

    DocumentErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public int errorCode() {
        return this.errorCode;
    }
}
