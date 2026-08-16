package com.mujin.document.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * API 文档完整模型（用于 PDF 导出）
 *
 * @author chenglin.wu
 * @date 2026/08/12
 */
@Data
public class ApiDocument {

    /**
     * 文档标题
     */
    private String title;

    /**
     * 文档版本
     */
    private String version;

    /**
     * 文档描述
     */
    private String description;

    /**
     * 生成时间
     */
    private String generatedAt;

    /**
     * 接口端点列表
     */
    private List<ApiEndpoint> endpoints = new ArrayList<>();

    /**
     * 数据模型列表
     */
    private List<ApiModel> models = new ArrayList<>();

    /**
     * 分组信息
     */
    private List<ApiGroup> groups = new ArrayList<>();

    /**
     * 标签信息
     */
    private List<ApiTag> tags = new ArrayList<>();

    /**
     * 安全方案列表（OAuth2/JWT/APIKey 等）
     */
    private List<ApiSecurityScheme> securitySchemes = new ArrayList<>();
}