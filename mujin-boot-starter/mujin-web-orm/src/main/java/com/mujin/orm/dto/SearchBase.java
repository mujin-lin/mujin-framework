package com.mujin.orm.dto;


import com.mujin.orm.entity.BaseEntity;

/**
 * 搜索查询的父类
 *
 * @author chenglin.wu
 * @date 2025/12/27 21:31
 */
@SuppressWarnings("rawtypes")
public abstract class SearchBase {
    /**
     * 获取基础的查询数据
     *
     * @return T
     * @date 2025/12/27
     */
    abstract <T extends BaseEntity> T getWrapper();
}
