package com.mujin.orm.handler;


import com.mujin.commons.lang.constants.IntConstants;
import com.mujin.orm.dto.AutoFillDto;

import java.time.LocalDateTime;

/**
 * 更新时间的自动填充处理类
 *
 * @author chenglin.wu
 * @date 2025/12/27 21:31
 */
public class UpdateTimeFillHandler implements InsertFillColumnHandler, UpdateFillColumnHandler {

    @Override
    public AutoFillDto insertFill() {
        return AutoFillDto.builder().fillColumnName("updateTime").fillVal(LocalDateTime.now()).fillClass(LocalDateTime.class).build();
    }


    @Override
    public AutoFillDto updateFill() {
        return AutoFillDto.builder().fillColumnName("updateTime").fillVal(LocalDateTime.now()).fillClass(LocalDateTime.class).build();
    }

    @Override
    public int getInsertFillOrder() {
        return IntConstants.INT_1;
    }


    @Override
    public int getUpdateFillOrder() {
        return 0;
    }
}
