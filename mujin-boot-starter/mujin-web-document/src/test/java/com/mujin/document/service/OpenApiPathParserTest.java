package com.mujin.document.service;

import com.mujin.document.model.ApiEndpoint;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * OpenApiPathParser 单元测试
 *
 * @author chenglin.wu
 * @date 2026/08/16
 */
class OpenApiPathParserTest {

    private OpenApiPathParser parser;

    @BeforeEach
    void setUp() {
        parser = new OpenApiPathParser(new OpenApiModelMapper());
    }

    @Test
    @DisplayName("parsePath：单个 GET 操作")
    void testParsePathSingleGet() {
        PathItem pathItem = new PathItem();
        Operation op = new Operation();
        op.setSummary("获取用户");
        op.setDescription("根据 ID 获取用户详情");
        op.setOperationId("user_getById");
        op.addTagsItem("user");
        pathItem.operation(io.swagger.v3.oas.models.PathItem.HttpMethod.GET, op);

        List<ApiEndpoint> endpoints = parser.parsePath("/users/{id}", pathItem, new OpenAPI());

        assertThat(endpoints).hasSize(1);
        ApiEndpoint e = endpoints.get(0);
        assertThat(e.getMethod()).isEqualTo("GET");
        assertThat(e.getPath()).isEqualTo("/users/{id}");
        assertThat(e.getSummary()).isEqualTo("获取用户");
        assertThat(e.getGroup()).isEqualTo("user");
    }

    @Test
    @DisplayName("parsePath：多 HTTP 方法 + path 级参数")
    void testParsePathMultiple() {
        PathItem pathItem = new PathItem();
        Operation get = new Operation();
        get.setOperationId("user_list");
        get.addTagsItem("user");
        pathItem.operation(PathItem.HttpMethod.GET, get);

        Operation post = new Operation();
        post.setOperationId("user_create");
        post.addTagsItem("user");
        pathItem.operation(PathItem.HttpMethod.POST, post);

        Parameter pageParam = new Parameter();
        pageParam.setName("page");
        pageParam.setIn("query");
        pathItem.addParametersItem(pageParam);

        List<ApiEndpoint> endpoints = parser.parsePath("/users", pathItem, new OpenAPI());

        assertThat(endpoints).hasSize(2);
        for (ApiEndpoint e : endpoints) {
            assertThat(e.getPath()).isEqualTo("/users");
            assertThat(e.getGroup()).isEqualTo("user");
            // path 级参数被加到所有 endpoint
            assertThat(e.getParameters()).hasSize(1);
            assertThat(e.getParameters().get(0).getName()).isEqualTo("page");
        }
    }

    @Test
    @DisplayName("parsePath：分组推断 - 优先 operationId 前缀")
    void testInferGroupFromOperationId() {
        PathItem pathItem = new PathItem();
        Operation op = new Operation();
        op.setOperationId("order_cancelById");
        pathItem.operation(PathItem.HttpMethod.POST, op);

        List<ApiEndpoint> endpoints = parser.parsePath("/orders/{id}/cancel", pathItem, new OpenAPI());

        assertThat(endpoints).hasSize(1);
        assertThat(endpoints.get(0).getGroup()).isEqualTo("order");
    }

    @Test
    @DisplayName("parsePath：分组推断 - fallback 到 tags 首项")
    void testInferGroupFromTags() {
        PathItem pathItem = new PathItem();
        Operation op = new Operation();
        op.addTagsItem("Product");
        pathItem.operation(PathItem.HttpMethod.GET, op);

        List<ApiEndpoint> endpoints = parser.parsePath("/products", pathItem, new OpenAPI());

        assertThat(endpoints).hasSize(1);
        assertThat(endpoints.get(0).getGroup()).isEqualTo("product");
    }

    @Test
    @DisplayName("parsePath：分组推断 - 兜底为 default")
    void testInferGroupDefault() {
        PathItem pathItem = new PathItem();
        Operation op = new Operation();
        pathItem.operation(PathItem.HttpMethod.GET, op);

        List<ApiEndpoint> endpoints = parser.parsePath("/", pathItem, new OpenAPI());

        assertThat(endpoints).hasSize(1);
        assertThat(endpoints.get(0).getGroup()).isEqualTo("default");
    }

    @Test
    @DisplayName("parsePath：operationMap 为 null 返回空列表")
    void testParsePathEmptyOperations() {
        PathItem pathItem = new PathItem();
        // 不添加任何 operation

        List<ApiEndpoint> endpoints = parser.parsePath("/empty", pathItem, new OpenAPI());

        assertThat(endpoints).isEmpty();
    }

    @Test
    @DisplayName("parsePath：deprecated=true 正确传递")
    void testParsePathDeprecated() {
        PathItem pathItem = new PathItem();
        Operation op = new Operation();
        op.setDeprecated(true);
        pathItem.operation(PathItem.HttpMethod.GET, op);

        List<ApiEndpoint> endpoints = parser.parsePath("/legacy", pathItem, new OpenAPI());

        assertThat(endpoints).hasSize(1);
        assertThat(endpoints.get(0).isDeprecated()).isTrue();
    }
}
