package com.mujin.orm.dto;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import java.util.List;

/**
 * 分页查询的基础类
 *
 * @author chenglin.wu
 * @date 2025/12/27
 */
@Data
@SuppressWarnings("unused")
public abstract class PageDto<T> {
    /**
     * 系统自带的分页对象
     */
    private Page<T> page;
    /**
     * 扩展字段
     */
    private List<PageExtra> extras;

    /**
     * 获取搜索条件
     *
     * @return S
     * @date 2025/12/27
     */
    public abstract <S> S getSearchExample();
}
