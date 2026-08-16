package com.mujin.document.auto;

import com.mujin.document.configuration.DocumentProperties;
import com.mujin.document.controller.DocumentController;
import com.mujin.document.service.CodeExampleGenerator;
import com.mujin.document.service.OpenApiParserService;
import com.mujin.document.service.PdfExportService;
import com.mujin.document.service.impl.PdfBoxPdfExportService;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

/**
 * 接口文档自动装配
 * <p>
 * 功能：
 * <ul>
 *   <li>启用 OpenAPI 3 文档生成（基于 springdoc 2.7，扫描整个应用上下文）</li>
 *   <li>配置 Swagger UI 自定义页面</li>
 *   <li>支持多模块分组（仅在用户显式配置时生效）</li>
 *   <li>提供 PDF 导出服务（基于 Apache PDFBox 3.x）</li>
 *   <li>提供文档控制器</li>
 * </ul>
 *
 * <p>插件化原则：本模块默认关闭（{@code mujin.document.enabled=false}），
 * 业务方需在 application.yml 显式启用。</p>
 *
 * @author chenglin.wu
 * @date 2026/08/16
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(DocumentProperties.class)
@ConditionalOnProperty(prefix = "mujin.document", name = "enabled", matchIfMissing = false)
public class DocumentAutoConfiguration implements WebMvcConfigurer {

    private final DocumentProperties properties;

    public DocumentAutoConfiguration(DocumentProperties properties) {
        this.properties = properties;
    }

    /**
     * 配置静态资源映射（Swagger UI 自定义页面）
     *
     * @param registry Spring MVC 资源注册器
     * @author chenglin.wu
     * @date 2026/08/16
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/doc.html")
                .addResourceLocations("classpath:/META-INF/resources/webjars/swagger-ui/doc.html");

        registry.addResourceHandler("/webjars/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/swagger-ui/");

        registry.addResourceHandler("/api-docs/**")
                .addResourceLocations("classpath:/META-INF/resources/api-docs/");
    }

    /**
     * OpenAPI 基础配置（Info / Contact / License / Server）
     *
     * @return OpenAPI 实例
     * @author chenglin.wu
     * @date 2026/08/16
     */
    @Bean
    @ConditionalOnMissingBean
    public OpenAPI openAPI() {
        Info info = new Info()
                .title(properties.getTitle())
                .version(properties.getVersion())
                .description(properties.getDescription())
                .termsOfService(properties.getTermsOfServiceUrl())
                .contact(new Contact()
                        .name(properties.getContact().getName())
                        .email(properties.getContact().getEmail())
                        .url(properties.getContact().getUrl()))
                .license(new License()
                        .name(properties.getLicense().getName())
                        .url(properties.getLicense().getUrl()));

        OpenAPI openAPI = new OpenAPI().info(info);

        Server server = new Server()
                .url("/")
                .description("默认服务器");
        openAPI.addServersItem(server);

        return openAPI;
    }

    /**
     * 多模块分组配置
     * <p>
     * 仅在用户显式配置 {@code mujin.document.groups[]} 时才创建 GroupedOpenApi Bean。
     * 未配置时 springdoc 会自动使用默认 {@code default} group 扫描整个 Spring 应用上下文，
     * 第三方包（如 com.jjj.xxx）的 @RestController 自动被扫描。
     *
     * @return List<GroupedOpenApi> 分组列表
     * @author chenglin.wu
     * @date 2026/08/16
     */
    @Bean
    @ConditionalOnMissingBean(name = "groupedOpenApis")
    @ConditionalOnProperty(prefix = "mujin.document", name = "groups")
    public List<GroupedOpenApi> groupedOpenApis() {
        List<GroupedOpenApi> apis = new ArrayList<>();
        if (properties.getGroups() == null || properties.getGroups().isEmpty()) {
            return apis;
        }

        for (DocumentProperties.GroupConfig group : properties.getGroups()) {
            GroupedOpenApi.Builder builder = GroupedOpenApi.builder()
                    .group(group.getName())
                    .displayName(group.getDisplayName());

            // 仅在用户显式配置 packagesToScan / pathsToMatch 时才设置过滤条件，
            // 否则由 springdoc 自动扫描整个应用上下文（包括第三方包）。
            if (group.getPackagesToScan() != null && !group.getPackagesToScan().isEmpty()) {
                builder.packagesToScan(group.getPackagesToScan().toArray(new String[0]));
            }
            if (group.getPathsToMatch() != null && !group.getPathsToMatch().isEmpty()) {
                builder.pathsToMatch(group.getPathsToMatch().toArray(new String[0]));
            }
            if (group.getPathsToExclude() != null && !group.getPathsToExclude().isEmpty()) {
                builder.pathsToExclude(group.getPathsToExclude().toArray(new String[0]));
            }

            apis.add(builder.build());
        }
        return apis;
    }

    /**
     * OpenAPI 解析服务
     *
     * @param mergedOpenApi   OpenAPI 根实例（springdoc 自动装配）
     * @param groupedOpenApis 分组 OpenAPI 配置列表（可选，可为空）
     * @return OpenApiParserService
     * @author chenglin.wu
     * @date 2026/08/16
     */
    @Bean
    @ConditionalOnMissingBean
    public OpenApiParserService openApiParserService(OpenAPI mergedOpenApi,
                                                      List<GroupedOpenApi> groupedOpenApis) {
        return new OpenApiParserService(mergedOpenApi, groupedOpenApis, properties);
    }

    /**
     * 代码示例生成器
     *
     * @return CodeExampleGenerator
     * @author chenglin.wu
     * @date 2026/08/16
     */
    @Bean
    @ConditionalOnMissingBean
    public CodeExampleGenerator codeExampleGenerator() {
        return new CodeExampleGenerator();
    }

    /**
     * PDF 导出服务（基于 Apache PDFBox 3.x）
     *
     * @return PdfExportService
     * @author chenglin.wu
     * @date 2026/08/16
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "mujin.document.pdf-export", name = "enabled", matchIfMissing = false)
    public PdfExportService pdfExportService() {
        return new PdfBoxPdfExportService(properties);
    }

    /**
     * 文档控制器
     *
     * @param parserService        OpenAPI 解析服务
     * @param pdfExportService     PDF 导出服务
     * @param codeExampleGenerator 代码示例生成器
     * @param groupedOpenApis      分组 OpenAPI 列表（可为空，springdoc 未启用分组时为空列表）
     * @return DocumentController
     * @author chenglin.wu
     * @date 2026/08/16
     */
    @Bean
    @ConditionalOnMissingBean
    public DocumentController documentController(OpenApiParserService parserService,
                                                  PdfExportService pdfExportService,
                                                  CodeExampleGenerator codeExampleGenerator,
                                                  List<GroupedOpenApi> groupedOpenApis) {
        return new DocumentController(parserService, pdfExportService, codeExampleGenerator, groupedOpenApis);
    }
}
