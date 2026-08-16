package com.mujin.document.service;

import com.mujin.document.model.ApiModel;
import com.mujin.document.model.ApiParameter;
import com.mujin.document.model.ApiResponse;
import com.mujin.document.model.ApiSecurityScheme;
import com.mujin.document.model.ApiTag;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * OpenApiModelMapper 单元测试
 *
 * @author chenglin.wu
 * @date 2026/08/16
 */
class OpenApiModelMapperTest {

    private OpenApiModelMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new OpenApiModelMapper();
    }

    @Test
    @DisplayName("convertTag：提取 name/description/externalDocs")
    void testConvertTag() {
        Tag tag = new Tag();
        tag.setName("用户");
        tag.setDescription("用户管理相关");
        ExternalDocumentation extDocs = new ExternalDocumentation();
        extDocs.setUrl("https://docs.example.com");
        extDocs.setDescription("用户文档");
        tag.setExternalDocs(extDocs);

        ApiTag result = mapper.convertTag(tag);

        assertThat(result.getName()).isEqualTo("用户");
        assertThat(result.getDescription()).isEqualTo("用户管理相关");
        assertThat(result.getExternalDocsUrl()).isEqualTo("https://docs.example.com");
        assertThat(result.getExternalDocsDescription()).isEqualTo("用户文档");
    }

    @Test
    @DisplayName("convertTag：externalDocs 为 null 不抛异常")
    void testConvertTagNoExternalDocs() {
        Tag tag = new Tag();
        tag.setName("test");

        ApiTag result = mapper.convertTag(tag);

        assertThat(result.getName()).isEqualTo("test");
        assertThat(result.getExternalDocsUrl()).isNull();
    }

    @Test
    @DisplayName("convertParameter：基本类型 + enum + required")
    void testConvertParameter() {
        Parameter param = new Parameter();
        param.setName("pageSize");
        param.setIn("query");
        param.setDescription("分页大小");
        param.setRequired(true);
        Schema<Integer> schema = new Schema<>();
        schema.setType("integer");
        schema.setFormat("int32");
        schema.addEnumItemObject(10);
        schema.addEnumItemObject(20);
        schema.addEnumItemObject(50);
        param.setSchema(schema);

        ApiParameter result = mapper.convertParameter(param);

        assertThat(result.getName()).isEqualTo("pageSize");
        assertThat(result.getIn()).isEqualTo("query");
        assertThat(result.isRequired()).isTrue();
        assertThat(result.getType()).isEqualTo("integer");
        assertThat(result.getFormat()).isEqualTo("int32");
        assertThat(result.getEnumValues()).containsExactly(10, 20, 50);
    }

    @Test
    @DisplayName("convertParameter：schema 为 null 时不抛异常")
    void testConvertParameterSchemaNull() {
        Parameter param = new Parameter();
        param.setName("test");
        param.setIn("query");

        ApiParameter result = mapper.convertParameter(param);

        assertThat(result.getName()).isEqualTo("test");
        assertThat(result.getType()).isNull();
    }

    @Test
    @DisplayName("convertRequestBody：$ref 类型")
    void testConvertRequestBodyRef() {
        RequestBody body = new RequestBody();
        body.setDescription("请求体");
        body.setRequired(true);
        Schema<?> schema = new Schema<>();
        schema.set$ref("#/components/schemas/UserDTO");
        Content content = new Content();
        MediaType mt = new MediaType();
        mt.setSchema(schema);
        content.addMediaType("application/json", mt);
        body.setContent(content);

        ApiParameter result = mapper.convertRequestBody(body, new OpenAPI());

        assertThat(result.getName()).isEqualTo("body");
        assertThat(result.getIn()).isEqualTo("body");
        assertThat(result.isRequired()).isTrue();
        assertThat(result.getSchemaRef()).isEqualTo("#/components/schemas/UserDTO");
    }

    @Test
    @DisplayName("convertResponses：含 headers 与 body 引用")
    void testConvertResponses() {
        io.swagger.v3.oas.models.responses.ApiResponse ok = new io.swagger.v3.oas.models.responses.ApiResponse();
        ok.setDescription("成功");
        Content content = new Content();
        MediaType mt = new MediaType();
        Schema<?> schema = new Schema<>();
        schema.set$ref("#/components/schemas/UserVO");
        mt.setSchema(schema);
        content.addMediaType("application/json", mt);
        ok.setContent(content);

        Header xRate = new Header();
        xRate.setSchema(new Schema<>().type("integer"));
        ok.addHeaderObject("X-Rate-Limit", xRate);

        ApiResponses responses = new ApiResponses();
        responses.addApiResponse("200", ok);

        List<ApiResponse> result = mapper.convertResponses(responses, new OpenAPI());

        assertThat(result).hasSize(1);
        ApiResponse r = result.get(0);
        assertThat(r.getStatusCode()).isEqualTo("200");
        assertThat(r.getDescription()).isEqualTo("成功");
        assertThat(r.getContentTypes()).contains("application/json");
        assertThat(r.getBody().getSchemaRef()).isEqualTo("#/components/schemas/UserVO");
        assertThat(r.getHeaders()).hasSize(1);
        assertThat(r.getHeaders().get(0).getName()).isEqualTo("X-Rate-Limit");
        assertThat(r.getHeaders().get(0).getType()).isEqualTo("integer");
    }

    @Test
    @DisplayName("convertModel：含 required 与 allOf 父类引用")
    void testConvertModel() {
        Schema<?> schema = new Schema<>();
        schema.setDescription("用户 DTO");
        schema.setType("object");
        schema.setRequired(List.of("name", "email"));

        Schema<?> parent = new Schema<>();
        parent.set$ref("#/components/schemas/BaseEntity");
        schema.setAllOf(List.of(parent));

        Map<String, Schema> properties = new HashMap<>();
        properties.put("name", new Schema<>().type("string").description("姓名"));
        properties.put("email", new Schema<>().type("string").description("邮箱"));
        schema.setProperties(properties);

        ApiModel model = mapper.convertModel("UserDTO", schema, properties);

        assertThat(model.getName()).isEqualTo("UserDTO");
        assertThat(model.getType()).isEqualTo("object");
        assertThat(model.getRequired()).containsExactly("name", "email");
        assertThat(model.getParentRef()).isEqualTo("BaseEntity");
        assertThat(model.getProperties()).hasSize(2);
    }

    @Test
    @DisplayName("convertSchemaProperties：含 array + $ref")
    void testConvertSchemaPropertiesArrayRef() {
        Schema<?> schema = new Schema<>();
        Map<String, Schema> properties = new HashMap<>();
        ArraySchema arraySchema = new ArraySchema();
        Schema<?> items = new Schema<>();
        items.set$ref("#/components/schemas/OrderDTO");
        arraySchema.setItems(items);
        properties.put("orders", arraySchema);
        schema.setProperties(properties);

        List<ApiParameter> result = mapper.convertSchemaProperties(schema, properties);

        assertThat(result).hasSize(1);
        ApiParameter orders = result.get(0);
        assertThat(orders.getName()).isEqualTo("orders");
        assertThat(orders.getItems()).isNotNull();
        assertThat(orders.getItems().getSchemaRef()).isEqualTo("OrderDTO");
    }

    @Test
    @DisplayName("convertSchemaProperties：properties 为 null 返回空列表")
    void testConvertSchemaPropertiesNull() {
        Schema<?> schema = new Schema<>();

        List<ApiParameter> result = mapper.convertSchemaProperties(schema, null);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("convertSecuritySchemes：从 Components 提取所有安全方案")
    void testConvertSecuritySchemes() {
        Components components = new Components();
        SecurityScheme bearer = new SecurityScheme();
        bearer.setType(SecurityScheme.Type.HTTP);
        bearer.setDescription("Bearer Token");
        bearer.setScheme("bearer");
        bearer.setBearerFormat("JWT");
        components.addSecuritySchemes("bearerAuth", bearer);

        List<ApiSecurityScheme> result = mapper.convertSecuritySchemes(components);

        assertThat(result).hasSize(1);
        ApiSecurityScheme s = result.get(0);
        assertThat(s.getName()).isEqualTo("bearerAuth");
        assertThat(s.getType()).isEqualTo("http");
        assertThat(s.getScheme()).isEqualTo("bearer");
        assertThat(s.getBearerFormat()).isEqualTo("JWT");
    }

    @Test
    @DisplayName("convertSecuritySchemes：components 为空时返回空列表")
    void testConvertSecuritySchemesEmpty() {
        assertThat(mapper.convertSecuritySchemes(null)).isEmpty();
        assertThat(mapper.convertSecuritySchemes(new Components())).isEmpty();
    }
}
