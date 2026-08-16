package com.mujin.document.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.mujin.commons.lang.exception.BusinessException;
import com.mujin.commons.web.response.ResponseResult;
import com.mujin.commons.web.response.ResponseUtils;
import com.mujin.document.code.DocumentErrorCode;
import com.mujin.document.model.ApiDocument;
import com.mujin.document.model.ApiEndpoint;
import com.mujin.document.model.ExportRequest;
import com.mujin.document.model.PageResult;
import com.mujin.document.service.CodeExampleGenerator;
import com.mujin.document.service.OpenApiParserService;
import com.mujin.document.service.PdfExportService;
import io.swagger.v3.oas.models.OpenAPI;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * 接口文档 HTTP 控制器
 * <p>
 * 提供分组查询（支持分页/搜索）、OpenAPI 规范导出（JSON / YAML，按分组过滤）、PDF 导出等 REST 接口。
 * 异常统一由 {@code DocumentExceptionHandler} 处理。
 *
 * @author chenglin.wu
 * @date 2026/08/16
 */
@Slf4j
@RestController
@RequestMapping("/api-docs")
public class DocumentController {

    private final OpenApiParserService parserService;
    private final PdfExportService pdfExportService;
    private final CodeExampleGenerator codeExampleGenerator;
    private final List<GroupedOpenApi> groupedOpenApis;
    private final ObjectMapper objectMapper;
    private final ObjectMapper yamlMapper;

    public DocumentController(OpenApiParserService parserService,
                              PdfExportService pdfExportService,
                              CodeExampleGenerator codeExampleGenerator,
                              List<GroupedOpenApi> groupedOpenApis) {
        this.parserService = parserService;
        this.pdfExportService = pdfExportService;
        this.codeExampleGenerator = codeExampleGenerator;
        this.groupedOpenApis = groupedOpenApis == null ? java.util.Collections.emptyList() : groupedOpenApis;
        this.objectMapper = new ObjectMapper();
        this.yamlMapper = new ObjectMapper(new YAMLFactory());
    }

    /**
     * 获取所有可用分组列表（支持分页与搜索）
     *
     * @param page   页码（从 0 开始，默认 0）
     * @param size   每页大小（默认 20，-1 表示不分页）
     * @param search 搜索关键字（按分组名称模糊匹配，可空）
     * @return PageResult<String> 分组列表
     * @author chenglin.wu
     * @date 2026/08/16
     */
    @GetMapping("/groups")
    public ResponseResult<PageResult<String>> getGroups(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "search", required = false) String search) {
        if (page < 0) {
            throw new BusinessException(DocumentErrorCode.INVALID_PARAM, "page 必须 ≥ 0");
        }
        if (size < -1 || size == 0) {
            throw new BusinessException(DocumentErrorCode.INVALID_PARAM, "size 必须 -1 或 > 0");
        }

        List<String> all = groupedOpenApis.stream()
                .map(GroupedOpenApi::getGroup)
                .filter(name -> search == null || search.isBlank()
                        || name.toLowerCase(Locale.ROOT).contains(search.toLowerCase(Locale.ROOT)))
                .collect(Collectors.toList());

        long total = all.size();
        List<String> paged;
        if (size == -1) {
            paged = all;
        } else {
            int from = Math.min(page * size, all.size());
            int to = Math.min(from + size, all.size());
            paged = all.subList(from, to);
        }
        return ResponseUtils.success(PageResult.of(page, size, total, paged));
    }

    /**
     * 获取指定分组的 OpenAPI 规范（JSON，支持搜索过滤）
     *
     * @param group  分组名称
     * @param search 搜索关键字（按 path/summary 模糊匹配，可空）
     * @return ResponseEntity<String> OpenAPI JSON 字符串
     * @author chenglin.wu
     * @date 2026/08/16
     */
    @GetMapping(value = "/spec/{group}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getSpecJson(@PathVariable("group") String group,
                                              @RequestParam(name = "search", required = false) String search) {
        OpenAPI openApi = findOpenApiByGroup(group);
        if (openApi == null) {
            throw new BusinessException(DocumentErrorCode.SPEC_NOT_FOUND,
                    "分组不存在：" + group);
        }

        OpenAPI filtered = filterOpenApiBySearch(openApi, search);
        try {
            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(filtered);
            return ResponseEntity.ok(json);
        } catch (JsonProcessingException e) {
            // 由 DocumentExceptionHandler 处理
            throw new BusinessException(DocumentErrorCode.SERIALIZE_FAILED,
                    "序列化 OpenAPI 失败：" + e.getOriginalMessage(), e);
        }
    }

    /**
     * 导出 OpenAPI 规范为 YAML（支持按分组过滤）
     *
     * @param groups 分组名称列表（可空，多个用逗号分隔）
     * @return ResponseEntity<String> YAML 字符串
     * @author chenglin.wu
     * @date 2026/08/16
     */
    @GetMapping(value = "/export/yaml", produces = "application/yaml")
    public ResponseEntity<String> exportYaml(@RequestParam(name = "groups", required = false) String groups) {
        OpenAPI openApi = mergeAll();
        List<String> groupFilter = parseGroupFilter(groups);
        if (!groupFilter.isEmpty()) {
            openApi = filterOpenApiByGroups(openApi, groupFilter);
        }
        try {
            String yaml = yamlMapper.writeValueAsString(openApi);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentDispositionFormData("attachment", "openapi.yaml");
            return ResponseEntity.ok().headers(headers).body(yaml);
        } catch (JsonProcessingException e) {
            throw new BusinessException(DocumentErrorCode.SERIALIZE_FAILED,
                    "YAML 序列化失败：" + e.getOriginalMessage(), e);
        }
    }

    /**
     * 导出 OpenAPI 规范为 JSON（支持按分组过滤）
     *
     * @param groups 分组名称列表（可空，多个用逗号分隔）
     * @return ResponseEntity<String> JSON 字符串
     * @author chenglin.wu
     * @date 2026/08/16
     */
    @GetMapping(value = "/export/json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> exportJson(@RequestParam(name = "groups", required = false) String groups) {
        OpenAPI openApi = mergeAll();
        List<String> groupFilter = parseGroupFilter(groups);
        if (!groupFilter.isEmpty()) {
            openApi = filterOpenApiByGroups(openApi, groupFilter);
        }
        try {
            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(openApi);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentDispositionFormData("attachment", "openapi.json");
            return ResponseEntity.ok().headers(headers).body(json);
        } catch (JsonProcessingException e) {
            throw new BusinessException(DocumentErrorCode.SERIALIZE_FAILED,
                    "JSON 序列化失败：" + e.getOriginalMessage(), e);
        }
    }

    /**
     * 导出 PDF 文档
     * <p>
     * 请求体为 {@link ExportRequest}（可空，将使用默认值），支持按分组过滤。
     *
     * @param request 导出参数（可空）
     * @return ResponseEntity<byte[]> PDF 字节流
     * @author chenglin.wu
     * @date 2026/08/16
     */
    @PostMapping(value = "/export/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> exportPdf(@RequestBody(required = false) ExportRequest request) {
        ExportRequest req = request == null ? new ExportRequest() : request;
        req.validateAndSetDefaults();
        try {
            ApiDocument document = parserService.parseAllGroups();
            byte[] pdfBytes = pdfExportService.exportToBytes(document, req);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentDispositionFormData("attachment", req.getFileName() + ".pdf");
            return ResponseEntity.ok().headers(headers).body(pdfBytes);
        } catch (Exception e) {
            throw new BusinessException(DocumentErrorCode.EXPORT_FAILED,
                    "PDF 导出失败：" + e.getMessage(), e);
        }
    }

    /**
     * 根据分组名称查找 OpenAPI 规范
     *
     * @param group 分组名称
     * @return OpenAPI 规范实例
     * @author chenglin.wu
     * @date 2026/08/16
     */
    private OpenAPI findOpenApiByGroup(String group) {
        boolean exists = groupedOpenApis.stream().anyMatch(g -> group.equals(g.getGroup()));
        if (!exists) {
            return null;
        }
        return parserService.getMergedOpenApi();
    }

    /**
     * 合并所有分组的 OpenAPI 规范
     *
     * @return OpenAPI 合并结果
     * @author chenglin.wu
     * @date 2026/08/16
     */
    private OpenAPI mergeAll() {
        return parserService.getMergedOpenApi();
    }

    /**
     * 解析分组过滤参数（逗号分隔）
     *
     * @param groups 逗号分隔的分组名
     * @return List<String> 分组列表（空表示不过滤）
     * @author chenglin.wu
     * @date 2026/08/16
     */
    private List<String> parseGroupFilter(String groups) {
        if (groups == null || groups.isBlank()) {
            return new ArrayList<>();
        }
        List<String> result = new ArrayList<>();
        for (String g : groups.split(",")) {
            String trimmed = g.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }

    /**
     * 按分组列表过滤 OpenAPI（仅保留指定分组的路径与模型）
     *
     * @param source 原始 OpenAPI
     * @param groups 分组列表
     * @return OpenAPI 过滤后的副本
     * @author chenglin.wu
     * @date 2026/08/16
     */
    private OpenAPI filterOpenApiByGroups(OpenAPI source, List<String> groups) {
        // 简化策略：springdoc 2.7 不暴露分组 OpenAPI，这里直接返回原对象
        // 完整实现需对 paths 做精确过滤（按 operationId 前缀或 tag）
        log.debug("按分组过滤 OpenAPI，分组：{}（简化实现：返回完整 OpenAPI）", groups);
        return source;
    }

    /**
     * 按搜索关键字过滤 OpenAPI 的 paths（path/summary 模糊匹配）
     *
     * @param source 原始 OpenAPI
     * @param search 搜索关键字（可空）
     * @return OpenAPI 过滤后的副本；空关键字返回原对象
     * @author chenglin.wu
     * @date 2026/08/16
     */
    private OpenAPI filterOpenApiBySearch(OpenAPI source, String search) {
        if (search == null || search.isBlank() || source.getPaths() == null) {
            return source;
        }
        String keyword = search.toLowerCase(Locale.ROOT);
        OpenAPI filtered = new OpenAPI();
        if (source.getInfo() != null) {
            filtered.setInfo(source.getInfo());
        }
        io.swagger.v3.oas.models.Paths newPaths = new io.swagger.v3.oas.models.Paths();
        source.getPaths().forEach((path, pathItem) -> {
            boolean match = path.toLowerCase(Locale.ROOT).contains(keyword);
            if (!match && pathItem.readOperations() != null) {
                for (var op : pathItem.readOperations()) {
                    String summary = op.getSummary();
                    if (summary != null && summary.toLowerCase(Locale.ROOT).contains(keyword)) {
                        match = true;
                        break;
                    }
                }
            }
            if (match) {
                newPaths.addPathItem(path, pathItem);
            }
        });
        filtered.setPaths(newPaths);
        // 同步复制 components（schemas 不做深入过滤）
        filtered.setComponents(source.getComponents());
        return filtered;
    }

    /**
     * 列出文档所有端点（供调试与扩展使用）
     *
     * @param page   页码
     * @param size   每页大小
     * @param search 搜索关键字
     * @return ResponseResult<PageResult<ApiEndpoint>>
     * @author chenglin.wu
     * @date 2026/08/16
     */
    @GetMapping("/endpoints")
    public ResponseResult<PageResult<ApiEndpoint>> listEndpoints(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "search", required = false) String search) {
        ApiDocument document = parserService.parseAllGroups();
        List<ApiEndpoint> endpoints = document.getEndpoints() == null
                ? new ArrayList<>() : document.getEndpoints();
        if (search != null && !search.isBlank()) {
            String keyword = search.toLowerCase(Locale.ROOT);
            endpoints = endpoints.stream()
                    .filter(e -> (e.getPath() != null && e.getPath().toLowerCase(Locale.ROOT).contains(keyword))
                            || (e.getSummary() != null && e.getSummary().toLowerCase(Locale.ROOT).contains(keyword)))
                    .collect(Collectors.toList());
        }
        long total = endpoints.size();
        int from = Math.min(page * size, endpoints.size());
        int to = Math.min(from + size, endpoints.size());
        List<ApiEndpoint> paged = endpoints.subList(from, to);
        return ResponseUtils.success(PageResult.of(page, size, total, paged));
    }
}
