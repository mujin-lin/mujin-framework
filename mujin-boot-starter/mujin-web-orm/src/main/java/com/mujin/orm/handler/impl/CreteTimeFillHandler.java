package com.mujin.orm.handler.impl;


import com.mujin.commons.lang.constants.IntConstants;
import com.mujin.orm.dto.AutoFillDto;
import com.mujin.orm.handler.InsertFillColumnHandler;

import java.time.LocalDateTime;

/**
 * 创建时间的自动填充处理类
 *
 * @author chenglin.wu
 * @date 2025/12/27 21:31
 */
public class CreteTimeFillHandler implements InsertFillColumnHandler {

    @Override
    public AutoFillDto insertFill() {
        return AutoFillDto.builder().fillColumnName("createTime").fillVal(LocalDateTime.now()).fillClass(LocalDateTime.class).build();
    }

    @Override
    public int getInsertFillOrder() {
        return IntConstants.INT_0;
    }
}
