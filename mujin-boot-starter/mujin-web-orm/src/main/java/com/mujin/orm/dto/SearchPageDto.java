package com.mujin.orm.dto;


import jakarta.validation.Valid;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * 利用其他类做查询搜索模板的分页查询类，返回结果也可以是其他类
 *
 * @author chenglin.wu
 * @date 2025/12/27
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SearchPageDto<T, S> extends PageDto<T> {
    /**
     * 搜索模板
     */
    @Valid
    private S searchExample;

}
