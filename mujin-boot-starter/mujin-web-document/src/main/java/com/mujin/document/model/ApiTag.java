package com.mujin.document.model;

import lombok.Data;

/**
 * API 标签模型
 *
 * @author chenglin.wu
 * @date 2026/08/12
 */
@Data
public class ApiTag {

    /**
     * 标签名称
     */
    private String name;

    /**
     * 标签描述
     */
    private String description;

    /**
     * 外部文档链接
     */
    private String externalDocsUrl;

    /**
     * 外部文档描述
     */
    private String externalDocsDescription;
}