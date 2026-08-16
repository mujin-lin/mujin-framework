package com.mujin.document.util;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * OpenAPI 规范相关工具方法
 * <p>
 * 提供 $ref 提取、Schema 基础判断、OpenAPI 合并等通用操作。
 * 全部方法为静态，无需实例化。
 *
 * @author chenglin.wu
 * @date 2026/08/16
 */
@SuppressWarnings("unused")
public final class OpenApiUtil {

    /**
     * 私有构造，禁止实例化
     *
     * @author chenglin.wu
     * @date 2026/08/16
     */
    private OpenApiUtil() {
    }

    /**
     * 从 $ref 字符串中提取模型名称
     * <p>
     * 例如 {@code "#/components/schemas/User"} 返回 {@code "User"}。
     *
     * @param ref $ref 引用字符串
     * @return String 模型名称；输入为空时返回 {@code null}
     * @author chenglin.wu
     * @date 2026/08/16
     */
    public static String extractRefName(String ref) {
        if (ref == null || ref.isEmpty()) {
            return null;
        }
        int slashIndex = ref.lastIndexOf('/');
        if (slashIndex < 0 || slashIndex == ref.length() - 1) {
            return ref;
        }
        return ref.substring(slashIndex + 1);
    }

    /**
     * 判断 Schema 是否为基本类型（不含 properties 的简单结构）
     *
     * @param schema OpenAPI Schema
     * @return boolean true 表示为基本类型
     * @author chenglin.wu
     * @date 2026/08/16
     */
    public static boolean isPrimitiveType(Schema<?> schema) {
        if (schema == null) {
            return true;
        }
        return schema.getProperties() == null || schema.getProperties().isEmpty();
    }

    /**
     * 判断 Schema 是否为数组类型
     *
     * @param schema OpenAPI Schema
     * @return boolean true 表示为数组
     * @author chenglin.wu
     * @date 2026/08/16
     */
    public static boolean isArrayType(Schema<?> schema) {
        if (schema == null) {
            return false;
        }
        return "array".equalsIgnoreCase(schema.getType());
    }

    /**
     * 合并多份 OpenAPI 规范
     * <p>
     * 将多个分组的 OpenAPI 合并到一份，paths/components/tags/servers 各自去重。
     *
     * @param sources OpenAPI 列表，允许为空
     * @return OpenAPI 合并后的规范；输入为空时返回空 OpenAPI 实例
     * @author chenglin.wu
     * @date 2026/08/16
     */
    public static OpenAPI mergeOpenApis(List<OpenAPI> sources) {
        OpenAPI merged = new OpenAPI();
        if (sources == null || sources.isEmpty()) {
            return merged;
        }

        Set<String> seenPaths = new HashSet<>();
        for (OpenAPI source : sources) {
            if (source == null) {
                continue;
            }
            mergePaths(source, merged, seenPaths);
            mergeComponents(source, merged);
            mergeTags(source, merged);
            mergeServers(source, merged);
        }
        return merged;
    }

    private static void mergePaths(OpenAPI source, OpenAPI merged, Set<String> seenPaths) {
        if (source.getPaths() == null) {
            return;
        }
        source.getPaths().forEach((path, pathItem) -> {
            if (seenPaths.add(path)) {
                if (merged.getPaths() == null) {
                    merged.setPaths(new io.swagger.v3.oas.models.Paths());
                }
                merged.getPaths().addPathItem(path, pathItem);
            }
        });
    }

    private static void mergeComponents(OpenAPI source, OpenAPI merged) {
        if (source.getComponents() == null || source.getComponents().getSchemas() == null) {
            return;
        }
        if (merged.getComponents() == null) {
            merged.setComponents(new io.swagger.v3.oas.models.Components());
        }
        if (merged.getComponents().getSchemas() == null) {
            merged.getComponents().setSchemas(new HashMap<>());
        }
        Map<String, Schema> sourceSchemas = source.getComponents().getSchemas();
        Map<String, Schema> targetSchemas = merged.getComponents().getSchemas();
        sourceSchemas.forEach(targetSchemas::putIfAbsent);
    }

    private static void mergeTags(OpenAPI source, OpenAPI merged) {
        if (source.getTags() == null) {
            return;
        }
        if (merged.getTags() == null) {
            merged.setTags(new ArrayList<>());
        }
        List<String> existingNames = new ArrayList<>();
        for (Tag tag : merged.getTags()) {
            existingNames.add(tag.getName());
        }
        source.getTags().forEach(tag -> {
            if (!existingNames.contains(tag.getName())) {
                merged.getTags().add(tag);
                existingNames.add(tag.getName());
            }
        });
    }

    private static void mergeServers(OpenAPI source, OpenAPI merged) {
        if (source.getServers() == null) {
            return;
        }
        if (merged.getServers() == null) {
            merged.setServers(new ArrayList<>());
        }
        List<String> existingUrls = new ArrayList<>();
        for (Server server : merged.getServers()) {
            existingUrls.add(server.getUrl());
        }
        source.getServers().forEach(server -> {
            if (!existingUrls.contains(server.getUrl())) {
                merged.getServers().add(server);
                existingUrls.add(server.getUrl());
            }
        });
    }
}
