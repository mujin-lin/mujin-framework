package com.mujin.orm.handler;


import com.mujin.orm.dto.AutoFillDto;

/**
 * 获取自动填充的数据，由用户手动定义
 *
 * @author chenglin.wu
 * @date 2025/12/27 21:31
 */
@FunctionalInterface
public interface FillSupplier {
    /**
     * 获取自动填充的数据信息
     *
     * @return AutoFillDto
     * @date 2025/12/27
     */
    AutoFillDto getFillDto();
}
