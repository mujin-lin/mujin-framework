package com.mujin.orm.configuration;


import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.mujin.orm.dto.AutoFillDto;
import org.apache.ibatis.reflection.MetaObject;

import java.util.Collection;
import java.util.List;

/**
 * 自动填充处理实现类
 *
 * @author chenglin.wu
 * @date 2025/12/27
 */
public class MybatisPlusMetaHandler implements MetaObjectHandler {

    private final Collection<AutoFillDto> sortedInsertFill;

    private final Collection<AutoFillDto> sortedUpdateFill;

    public MybatisPlusMetaHandler(List<AutoFillDto> insertAutoFill, List<AutoFillDto> updateAutFill) {

        this.sortedInsertFill = insertAutoFill;
        this.sortedUpdateFill = updateAutFill;


    }

    @Override
    public void insertFill(MetaObject metaObject) {
        if (this.sortedInsertFill.isEmpty()) {
            return;
        }
        for (AutoFillDto fillDto : this.sortedInsertFill) {
            this.strictInsertFill(metaObject, fillDto.getFillColumnName(), fillDto.getFillClass(), fillDto.getFillVal());
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        if (this.sortedUpdateFill.isEmpty()) {
            return;
        }
        for (AutoFillDto fillDto : this.sortedUpdateFill) {
            this.strictUpdateFill(metaObject, fillDto.getFillColumnName(), fillDto.getFillClass(), fillDto.getFillVal());
        }
    }
}
