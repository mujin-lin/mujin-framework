package com.mujin.document.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 接口文档配置属性
 *
 * @author chenglin.wu
 * @date 2026/08/12
 */
@Data
@ConfigurationProperties(prefix = "mujin.document")
public class DocumentProperties {

    /**
     * 是否启用接口文档功能（默认关闭，符合插件化"按需启用"原则）
     */
    private boolean enabled = false;

    /**
     * 文档标题
     */
    private String title = "Mujin Framework API 文档";

    /**
     * 文档版本
     */
    private String version = "1.0.0";

    /**
     * 文档描述
     */
    private String description = "基于 OpenAPI 3.0 规范自动生成的接口文档";

    /**
     * 服务条款 URL
     */
    private String termsOfServiceUrl = "";

    /**
     * 联系人信息
     */
    private Contact contact = new Contact();

    /**
     * 许可证信息
     */
    private License license = new License();

    /**
     * 基础包扫描路径（用于 OpenAPI 分组）
     * <p>
     * 已废弃：自 1.1.0 起扫描逻辑由 springdoc 自动处理，业务方无需再指定 basePackages。
     * 如需自定义分组，请使用 {@link GroupConfig#packagesToScan}。
     *
     * @deprecated since 1.1.0, replaced by {@code groups[].packagesToScan}
     */
    @Deprecated
    private List<String> basePackages = new ArrayList<>();

    /**
     * 需要排除的路径模式
     * <p>
     * 已废弃：自 1.1.0 起排除逻辑由 springdoc 自动处理，业务方无需再指定 excludedPaths。
     * 如需自定义分组，请使用 {@link GroupConfig#pathsToExclude}。
     *
     * @deprecated since 1.1.0, replaced by {@code groups[].pathsToExclude}
     */
    @Deprecated
    private List<String> excludedPaths = new ArrayList<>();

    /**
     * Swagger UI 配置
     */
    private SwaggerUi swaggerUi = new SwaggerUi();

    /**
     * PDF 导出配置
     */
    private PdfExport pdfExport = new PdfExport();

    /**
     * 解析缓存配置（OpenAPI → ApiDocument 缓存）
     */
    private CacheConfig cache = new CacheConfig();

    /**
     * 分组配置（多模块项目支持）
     */
    private List<GroupConfig> groups = new ArrayList<>();

    @Data
    public static class Contact {
        /**
         * 联系人名称
         */
        private String name = "Mujin Team";

        /**
         * 联系人邮箱
         */
        private String email = "support@mujin.com";

        /**
         * 联系人 URL
         */
        private String url = "https://gitee.com/mujin/mujin-framework";
    }

    @Data
    public static class License {
        /**
         * 许可证名称
         */
        private String name = "Apache 2.0";

        /**
         * 许可证 URL
         */
        private String url = "https://www.apache.org/licenses/LICENSE-2.0.html";
    }

    @Data
    public static class SwaggerUi {
        /**
         * Swagger UI 路径前缀
         */
        private String path = "/doc.html";

        /**
         * 是否启用 Swagger UI
         */
        private boolean enabled = true;
        /**
         * 是否显示扩展按钮
         */
        private boolean displayOperationId = false;

        /**
         * 默认展开模式：list / full / none
         */
        private String docExpansion = "list";

        /**
         * 是否启用过滤器
         */
        private boolean filter = true;

        /**
         * 是否显示请求持续时间
         */
        private boolean displayRequestDuration = true;

        /**
         * 主题配色
         */
        private String theme = "classic";

        /**
         * 自定义 CSS
         */
        private String customCss = "";

        /**
         * 自定义 JS
         */
        private String customJs = "";

        /**
         * 自定义站点标题
         */
        private String siteTitle = "";

        /**
         * 自定义 favicon
         */
        private String favicon = "";
    }

    @Data
    public static class PdfExport {
        /**
         * 是否启用 PDF 导出（默认关闭，符合插件化原则）
         */
        private boolean enabled = false;

        /**
         * PDF 导出路径
         */
        private String path = "/api-docs/pdf";

        /**
         * PDF 生成引擎：PDFBOX
         */
        private String engine = "PDFBOX";

        /**
         * 中文字体文件路径（TTF/OTF），为空时降级到 PDFBox 内置 Helvetica（无中文支持）
         */
        private String fontPath = "";

        /**
         * 输出目录
         */
        private String outputDir = "target/api-docs";

        /**
         * 文件名前缀
         */
        private String fileNamePrefix = "api-document";

        /**
         * 是否包含调用示例
         */
        private boolean includeExamples = true;

        /**
         * 是否包含数据模型
         */
        private boolean includeModels = true;

        /**
         * 纸张大小：A4 / LETTER
         */
        private String pageSize = "A4";

        /**
         * 页面边距（mm）
         */
        private int margin = 20;
    }

    @Data
    public static class CacheConfig {
        /**
         * 是否启用 OpenAPI → ApiDocument 解析缓存
         */
        private boolean enabled = true;

        /**
         * 缓存过期时间（秒）
         */
        private int ttlSeconds = 300;

        /**
         * 缓存最大条目数
         */
        private int maxSize = 100;
    }

    @Data
    public static class GroupConfig {
        /**
         * 分组名称
         */
        private String name;

        /**
         * 分组显示名称
         */
        private String displayName;

        /**
         * 包含的包路径
         */
        private List<String> packagesToScan = new ArrayList<>();

        /**
         * 包含的路径模式
         */
        private List<String> pathsToMatch = new ArrayList<>();

        /**
         * 排除的路径模式
         */
        private List<String> pathsToExclude = new ArrayList<>();

        /**
         * 分组排序
         */
        private int order = 0;
    }
}