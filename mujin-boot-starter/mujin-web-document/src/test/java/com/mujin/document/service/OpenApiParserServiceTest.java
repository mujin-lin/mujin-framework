package com.mujin.document.service;

import com.mujin.document.configuration.DocumentProperties;
import com.mujin.document.model.ApiDocument;
import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springdoc.core.models.GroupedOpenApi;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * OpenApiParserService 单元测试（缓存行为）
 *
 * @author chenglin.wu
 * @date 2026/08/16
 */
class OpenApiParserServiceTest {

    private OpenApiParserService service;

    @BeforeEach
    void setUp() {
        DocumentProperties props = new DocumentProperties();
        props.getCache().setEnabled(true);
        props.getCache().setTtlSeconds(60);
        props.getCache().setMaxSize(10);

        OpenAPI openApi = new OpenAPI();
        openApi.setInfo(new io.swagger.v3.oas.models.info.Info()
                .title("Test API")
                .version("1.0")
                .description("Test"));

        service = new OpenApiParserService(openApi, new ArrayList<>(), props);
    }

    @Test
    @DisplayName("parseAllGroups：基本信息填充")
    void testParseAllGroupsBasicInfo() {
        ApiDocument doc = service.parseAllGroups();
        assertThat(doc.getTitle()).isEqualTo("Test API");
        assertThat(doc.getVersion()).isEqualTo("1.0");
        assertThat(doc.getDescription()).isEqualTo("Test");
    }

    @Test
    @DisplayName("parseAllGroups：相同输入返回同一实例（命中缓存）")
    void testParseAllGroupsCached() {
        ApiDocument first = service.parseAllGroups();
        ApiDocument second = service.parseAllGroups();
        // 缓存命中时返回同一实例
        assertThat(second).isSameAs(first);
    }

    @Test
    @DisplayName("invalidateCache：清空后重新解析")
    void testInvalidateCache() {
        ApiDocument first = service.parseAllGroups();
        service.invalidateCache();
        ApiDocument second = service.parseAllGroups();
        assertThat(second).isNotSameAs(first);
    }

    @Test
    @DisplayName("getAvailableGroups：空分组列表返回空")
    void testGetAvailableGroupsEmpty() {
        assertThat(service.getAvailableGroups()).isEmpty();
    }

    @Test
    @DisplayName("parseGroup：返回全量文档（springdoc 2.7 简化行为）")
    void testParseGroup() {
        ApiDocument doc = service.parseGroup("any-group");
        assertThat(doc).isNotNull();
        assertThat(doc.getTitle()).isEqualTo("Test API");
    }

    @Test
    @DisplayName("getMergedOpenApi：返回原始实例")
    void testGetMergedOpenApi() {
        assertThat(service.getMergedOpenApi()).isNotNull();
        assertThat(service.getMergedOpenApi().getInfo().getTitle()).isEqualTo("Test API");
    }

    @Test
    @DisplayName("构造时 null OpenAPI：使用空实例不抛异常")
    void testNullOpenApi() {
        DocumentProperties props = new DocumentProperties();
        OpenApiParserService svc = new OpenApiParserService(null, null, props);
        ApiDocument doc = svc.parseAllGroups();
        assertThat(doc).isNotNull();
    }
}
