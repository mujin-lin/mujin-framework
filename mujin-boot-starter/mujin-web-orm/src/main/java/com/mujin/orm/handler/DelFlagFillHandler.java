package com.mujin.orm.handler;


import com.mujin.commons.lang.constants.IntConstants;
import com.mujin.orm.dto.AutoFillDto;

/**
 * 删除标志的自动填充处理
 *
 * @author chenglin.wu
 * @date 2025/12/27 21:31
 */
public class DelFlagFillHandler implements InsertFillColumnHandler {

    @Override
    public AutoFillDto insertFill() {
        return AutoFillDto.builder().fillColumnName("delFlag").fillVal(0).fillClass(Integer.class).build();
    }

    @Override
    public int getInsertFillOrder() {
        return IntConstants.INT_2;
    }
}
