package com.mujin.orm.handler;

import com.mujin.orm.dto.AutoFillDto;

/**
 * 自动插入字段的处理类，由用户手动实现
 *
 * @author chenglin.wu
 * @date 2025/12/27 21:31
 */
public interface InsertFillColumnHandler {
    /**
     * 获取插入时自动数据的方法
     *
     * @return AutoFillDto
     * @date 2025/12/27
     */
    AutoFillDto insertFill();

    /**
     * 获取 insert 自动填充时的顺序
     *
     * @return int
     * @date 2025/12/27
     */
    int getInsertFillOrder();
}
