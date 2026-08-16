package com.mujin.document.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.mujin.document.configuration.DocumentProperties;
import com.mujin.document.model.ApiDocument;
import com.mujin.document.model.ApiEndpoint;
import com.mujin.document.model.ApiGroup;
import com.mujin.document.model.ApiModel;
import com.mujin.document.model.ApiSecurityScheme;
import com.mujin.document.model.ApiTag;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.models.GroupedOpenApi;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * OpenAPI 解析编排服务
 * <p>
 * 仅负责顶层编排：OpenAPI → tags / paths / components → {@link ApiDocument}。
 * 具体 DTO 映射由 {@link OpenApiModelMapper} 完成，
 * 单路径解析由 {@link OpenApiPathParser} 完成。
 * <p>
 * 通过 Caffeine 缓存解析结果，避免每次请求都重新遍历 OpenAPI。
 * 缓存 key 由 OpenAPI 的 hashCode + 分组列表 size 组成，命中后解析耗时 < 5ms。
 *
 * <p>springdoc 2.7 中 {@link GroupedOpenApi} 不再暴露 {@code getOpenApi()}
 * 方法，因此解析操作统一作用于由 {@code DocumentAutoConfiguration} 注册的合并 {@link OpenAPI} Bean。</p>
 *
 * @author chenglin.wu
 * @date 2026/08/16
 */
@Slf4j
@SuppressWarnings("unused")
public class OpenApiParserService {

    /**
     * OpenAPI 合并实例，由 springdoc 自动装配
     */
    private final OpenAPI mergedOpenApi;

    /**
     * 分组 OpenAPI 配置列表（仅用于展示分组元数据）
     */
    private final List<GroupedOpenApi> groupedOpenApis;

    /**
     * DTO 映射器
     */
    private final OpenApiModelMapper modelMapper;

    /**
     * 路径解析器
     */
    private final OpenApiPathParser pathParser;

    /**
     * 解析结果缓存（key = OpenAPI + groups 的 hash，value = ApiDocument）
     */
    private final Cache<Integer, ApiDocument> cache;

    public OpenApiParserService(OpenAPI mergedOpenApi,
                                List<GroupedOpenApi> groupedOpenApis,
                                DocumentProperties properties) {
        this.mergedOpenApi = mergedOpenApi == null ? new OpenAPI() : mergedOpenApi;
        this.groupedOpenApis = groupedOpenApis == null ? new ArrayList<>() : groupedOpenApis;
        this.modelMapper = new OpenApiModelMapper();
        this.pathParser = new OpenApiPathParser(modelMapper);

        DocumentProperties.CacheConfig cacheCfg = properties != null
                ? properties.getCache() : new DocumentProperties.CacheConfig();
        if (cacheCfg == null) {
            cacheCfg = new DocumentProperties.CacheConfig();
        }
        int ttl = cacheCfg.getTtlSeconds() > 0 ? cacheCfg.getTtlSeconds() : 300;
        int maxSize = cacheCfg.getMaxSize() > 0 ? cacheCfg.getMaxSize() : 100;
        boolean enabled = cacheCfg.isEnabled();
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(ttl))
                .maximumSize(maxSize)
                .build();
        log.info("OpenApiParserService 初始化完成，缓存启用：{}，TTL：{}s，最大条目：{}",
                enabled, ttl, maxSize);
    }

    /**
     * 获取底层合并后的 OpenAPI 实例
     *
     * @return OpenAPI
     * @author chenglin.wu
     * @date 2026/08/16
     */
    public OpenAPI getMergedOpenApi() {
        return mergedOpenApi;
    }

    /**
     * 解析所有分组的 OpenAPI 规范，生成统一的 ApiDocument
     *
     * @return ApiDocument 文档模型
     * @author chenglin.wu
     * @date 2026/08/16
     */
    public ApiDocument parseAllGroups() {
        Integer key = computeCacheKey();
        return cache.get(key, k -> doParseAllGroups());
    }

    /**
     * 实际执行解析（无缓存）
     *
     * @return ApiDocument 文档模型
     * @author chenglin.wu
     * @date 2026/08/16
     */
    private ApiDocument doParseAllGroups() {
        ApiDocument document = new ApiDocument();
        document.setTitle("API 接口文档");
        document.setVersion("1.0.0");
        document.setDescription("自动生成的接口文档");
        document.setGeneratedAt(java.time.LocalDateTime.now().toString());

        if (mergedOpenApi.getInfo() != null) {
            document.setTitle(mergedOpenApi.getInfo().getTitle());
            document.setVersion(mergedOpenApi.getInfo().getVersion());
            document.setDescription(mergedOpenApi.getInfo().getDescription());
        }

        if (mergedOpenApi.getTags() != null) {
            List<ApiTag> tags = new ArrayList<>();
            for (Tag tag : mergedOpenApi.getTags()) {
                tags.add(modelMapper.convertTag(tag));
            }
            document.setTags(tags);
        }

        if (mergedOpenApi.getPaths() != null) {
            List<ApiEndpoint> endpoints = new ArrayList<>();
            Map<String, PathItem> paths = mergedOpenApi.getPaths();
            paths.forEach((path, pathItem) -> endpoints.addAll(pathParser.parsePath(path, pathItem, mergedOpenApi)));
            document.setEndpoints(endpoints);
        }

        if (mergedOpenApi.getComponents() != null) {
            if (mergedOpenApi.getComponents().getSchemas() != null) {
                Map<String, Schema> schemas = mergedOpenApi.getComponents().getSchemas();
                List<ApiModel> models = new ArrayList<>();
                for (Map.Entry<String, Schema> entry : schemas.entrySet()) {
                    models.add(modelMapper.convertModel(entry.getKey(), entry.getValue(), schemas));
                }
                document.setModels(models);
            }

            List<ApiSecurityScheme> securitySchemes = modelMapper.convertSecuritySchemes(mergedOpenApi.getComponents());
            document.setSecuritySchemes(securitySchemes);
        }

        if (!groupedOpenApis.isEmpty()) {
            List<ApiGroup> groups = new ArrayList<>();
            for (GroupedOpenApi grouped : groupedOpenApis) {
                ApiGroup group = new ApiGroup();
                group.setName(grouped.getGroup());
                group.setDisplayName(grouped.getDisplayName());
                group.setOrder(0);
                groups.add(group);
            }
            document.setGroups(groups);
        }

        return document;
    }

    /**
     * 计算缓存 key：基于 OpenAPI 的 hash 与分组数量
     * <p>
     * 当 OpenAPI 实例发生变化（springdoc 重新扫描）时 hash 会自动失效。
     * 分组列表 size 影响分组元数据展示。
     *
     * @return Integer 缓存 key
     * @author chenglin.wu
     * @date 2026/08/16
     */
    private Integer computeCacheKey() {
        int openApiHash = mergedOpenApi == null ? 0 : mergedOpenApi.hashCode();
        int groupsHash = groupedOpenApis == null ? 0 : groupedOpenApis.size();
        return openApiHash * 31 + groupsHash;
    }

    /**
     * 解析指定分组的 OpenAPI 规范
     * <p>
     * springdoc 2.7 不再暴露 GroupedOpenApi 的运行时 OpenAPI，
     * 这里返回基于全局 OpenAPI 的简版文档供前端展示。
     *
     * @param groupName 分组名称（仅用于日志记录）
     * @return ApiDocument 文档模型
     * @author chenglin.wu
     * @date 2026/08/16
     */
    public ApiDocument parseGroup(String groupName) {
        log.info("按分组解析文档，分组名称：{}（springdoc 2.7 下分组 OpenAPI 通过 /v3/api-docs/{group} 获取）", groupName);
        return parseAllGroups();
    }

    /**
     * 获取可用分组列表
     *
     * @return List<String> 分组名称集合
     * @author chenglin.wu
     * @date 2026/08/16
     */
    public List<String> getAvailableGroups() {
        List<String> groups = new ArrayList<>();
        for (GroupedOpenApi grouped : groupedOpenApis) {
            groups.add(grouped.getGroup());
        }
        return groups;
    }

    /**
     * 清空解析缓存（用于配置热更新后强制重新解析）
     *
     * @author chenglin.wu
     * @date 2026/08/16
     */
    public void invalidateCache() {
        cache.invalidateAll();
        log.info("OpenApiParserService 缓存已清空");
    }
}
