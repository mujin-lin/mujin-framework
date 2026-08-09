package com.mujin.orm.dto;


import com.mujin.orm.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * 利用实体类做搜索模版的分页查询
 *
 * @author chenglin.wu
 * @date 2025/12/27
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuppressWarnings("rawtypes")
public class EntityPageDto<T extends BaseEntity> extends PageDto<T> {
    /**
     * 查询模板是实体类
     */
    private T searchExample;

}
