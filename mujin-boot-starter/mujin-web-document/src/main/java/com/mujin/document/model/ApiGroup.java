package com.mujin.document.model;

import lombok.Data;

/**
 * API 分组模型
 *
 * @author chenglin.wu
 * @date 2026/08/12
 */
@Data
public class ApiGroup {

    /**
     * 分组名称
     */
    private String name;

    /**
     * 显示名称
     */
    private String displayName;

    /**
     * 分组描述
     */
    private String description;

    /**
     * 排序
     */
    private int order;
}