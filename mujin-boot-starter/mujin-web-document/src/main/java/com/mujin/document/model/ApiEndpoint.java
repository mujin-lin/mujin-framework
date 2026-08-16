package com.mujin.document.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * API 端点模型
 *
 * @author chenglin.wu
 * @date 2026/08/12
 */
@Data
public class ApiEndpoint {

    /**
     * HTTP 方法
     */
    private String method;

    /**
     * 路径
     */
    private String path;

    /**
     * 摘要
     */
    private String summary;

    /**
     * 详细描述
     */
    private String description;

    /**
     * 操作 ID
     */
    private String operationId;

    /**
     * 标签列表
     */
    private List<String> tags = new ArrayList<>();

    /**
     * 分组名称
     */
    private String group;

    /**
     * 是否弃用
     */
    private boolean deprecated;

    /**
     * 请求参数列表
     */
    private List<ApiParameter> parameters = new ArrayList<>();

    /**
     * 请求体
     */
    private ApiParameter requestBody;

    /**
     * 响应列表
     */
    private List<ApiResponse> responses = new ArrayList<>();

    /**
     * 安全要求
     */
    private List<String> security = new ArrayList<>();

    /**
     * 代码示例
     */
    private List<CodeExample> examples = new ArrayList<>();
}