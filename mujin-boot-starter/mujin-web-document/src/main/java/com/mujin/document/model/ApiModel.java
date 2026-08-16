package com.mujin.document.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * API 数据模型
 *
 * @author chenglin.wu
 * @date 2026/08/12
 */
@Data
public class ApiModel {

    /**
     * 模型名称
     */
    private String name;

    /**
     * 模型描述
     */
    private String description;

    /**
     * 模型类型
     */
    private String type;

    /**
     * 属性列表
     */
    private List<ApiParameter> properties = new ArrayList<>();

    /**
     * 必填字段列表
     */
    private List<String> required = new ArrayList<>();

    /**
     * 父模型（继承）
     */
    private String parentRef;

    /**
     * 判别器（多态）
     */
    private String discriminator;

    /**
     * 示例值
     */
    private Object example;

    /**
     * 扩展属性
     */
    private Map<String, Object> extensions;
}