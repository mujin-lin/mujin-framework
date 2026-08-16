package com.mujin.document.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * API 参数模型
 *
 * @author chenglin.wu
 * @date 2026/08/12
 */
@Data
public class ApiParameter {

    /**
     * 参数名称
     */
    private String name;

    /**
     * 参数位置：query / header / path / cookie
     */
    private String in;

    /**
     * 参数描述
     */
    private String description;

    /**
     * 是否必填
     */
    private boolean required;

    /**
     * 是否弃用
     */
    private boolean deprecated;

    /**
     * 参数类型
     */
    private String type;

    /**
     * 参数格式
     */
    private String format;

    /**
     * 示例值
     */
    private Object example;

    /**
     * 枚举值
     */
    private List<Object> enumValues = new ArrayList<>();

    /**
     * 默认值
     */
    private Object defaultValue;

    /**
     * Schema 引用（复杂类型）
     */
    private String schemaRef;

    /**
     * 嵌套属性（对象类型）
     */
    private List<ApiParameter> properties = new ArrayList<>();

    /**
     * 数组项类型
     */
    private ApiParameter items;

    /**
     * 扩展属性
     */
    private Map<String, Object> extensions;
}