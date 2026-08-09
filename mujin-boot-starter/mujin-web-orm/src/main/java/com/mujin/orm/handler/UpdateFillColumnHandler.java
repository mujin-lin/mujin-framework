package com.mujin.orm.handler;

import com.mujin.orm.dto.AutoFillDto;

/**
 * 自动更新列的处理类，由用户自己事项
 *
 * @author chenglin.wu
 * @date 2025/12/27 21:31
 */
public interface UpdateFillColumnHandler {
    /**
     * 获取更新时自动数据的方法
     *
     * @return AutoFillDto
     * @date 2025/12/27
     */
    AutoFillDto updateFill();

    /**
     * 获取 update 自动填充时的顺序
     *
     * @return int
     * @date 2025/12/27
     */
    int getUpdateFillOrder();
}
