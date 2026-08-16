package com.mujin.document.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * API 响应模型
 *
 * @author chenglin.wu
 * @date 2026/08/12
 */
@Data
public class ApiResponse {

    /**
     * HTTP 状态码
     */
    private String statusCode;

    /**
     * 响应描述
     */
    private String description;

    /**
     * 响应内容类型
     */
    private List<String> contentTypes = new ArrayList<>();

    /**
     * 响应体参数
     */
    private ApiParameter body;

    /**
     * 响应头
     */
    private List<ApiParameter> headers = new ArrayList<>();

    /**
     * 扩展属性
     */
    private Map<String, Object> extensions;
}