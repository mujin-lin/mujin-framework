package com.mujin.commons.web.enums.error;

import com.mujin.commons.lang.code.DataCheckErrorCode;

/**
 * 数据异常
 *
 * @author chenglin.wu
 * @date 2025/12/6
 */
public enum DataError implements DataCheckErrorCode {
    /**
     * 未满足数据校验
     */
    DATA_CHECK(601);

    private final int errorCode;

    DataError(final int errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public int errorCode() {
        return this.errorCode;
    }
}
