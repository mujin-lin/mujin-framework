package com.mujin.orm.entity;


import lombok.Data;

import java.io.Serializable;

/**
 * 基础实体类
 *
 * @author chenglin.wu
 * @date 2025/12/27
 */
@Data
@SuppressWarnings("unused")
public abstract class BaseEntity<ID extends Serializable> {

    /**
     * 获取 id
     *
     * @return ID
     */
    public abstract ID getId();

    /**
     * 设置id
     *
     * @param id id
     * @date 2025/12/27
     */
    public abstract void setId(ID id);
}