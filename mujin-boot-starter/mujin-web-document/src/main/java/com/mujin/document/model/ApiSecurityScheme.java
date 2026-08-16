package com.mujin.document.model;

import lombok.Data;

/**
 * API 安全方案 DTO
 * <p>
 * 承载 OpenAPI {@code components.securitySchemes} 中的 OAuth2/JWT/APIKey 等通用字段。
 * OAuth flow 详情暂不展开（按需扩展）。
 *
 * @author chenglin.wu
 * @date 2026/08/16
 */
@Data
public class ApiSecurityScheme {

    /**
     * 安全方案名（Map key）
     */
    private String name;

    /**
     * 类型：apiKey / http / oauth2 / openIdConnect / mutualTLS
     */
    private String type;

    /**
     * 描述
     */
    private String description;

    /**
     * HTTP scheme：basic / bearer / digest 等
     */
    private String scheme;

    /**
     * Bearer Token 格式
     */
    private String bearerFormat;

    /**
     * OpenID Connect URL
     */
    private String openIdConnectUrl;
}
