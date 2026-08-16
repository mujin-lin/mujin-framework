package com.mujin.document.service;

import com.mujin.document.model.ApiModel;
import com.mujin.document.model.ApiParameter;
import com.mujin.document.model.ApiResponse;
import com.mujin.document.model.ApiTag;
import com.mujin.document.util.OpenApiUtil;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.tags.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * OpenAPI 规范 → 自定义 DTO 模型映射器
 * <p>
 * 仅承担"模型转换"职责，不做缓存、不做编排。
 * 调用方为 {@link OpenApiParserService} 与 {@link OpenApiPathParser}。
 *
 * @author chenglin.wu
 * @date 2026/08/16
 */
@SuppressWarnings("unchecked")
public class OpenApiModelMapper {

    /**
     * 转换 OpenAPI 标签
     *
     * @param tag OpenAPI 标签
     * @return ApiTag 自定义标签模型
     * @author chenglin.wu
     * @date 2026/08/16
     */
    public ApiTag convertTag(Tag tag) {
        ApiTag apiTag = new ApiTag();
        apiTag.setName(tag.getName());
        apiTag.setDescription(tag.getDescription());
        if (tag.getExternalDocs() != null) {
            apiTag.setExternalDocsUrl(tag.getExternalDocs().getUrl());
            apiTag.setExternalDocsDescription(tag.getExternalDocs().getDescription());
        }
        return apiTag;
    }

    /**
     * 转换 OpenAPI 参数
     *
     * @param parameter OpenAPI Parameter
     * @return ApiParameter 自定义参数模型
     * @author chenglin.wu
     * @date 2026/08/16
     */
    public ApiParameter convertParameter(Parameter parameter) {
        ApiParameter param = new ApiParameter();
        param.setName(parameter.getName());
        param.setIn(parameter.getIn());
        param.setDescription(parameter.getDescription());
        param.setRequired(parameter.getRequired() != null && parameter.getRequired());
        param.setDeprecated(parameter.getDeprecated() != null && parameter.getDeprecated());
        param.setExample(parameter.getExample());

        if (parameter.getSchema() != null) {
            Schema schema = parameter.getSchema();
            param.setType(schema.getType());
            param.setFormat(schema.getFormat());
            if (schema.getEnum() != null) {
                List<Object> enumValues = (List<Object>) schema.getEnum();
                param.setEnumValues(new ArrayList<>(enumValues));
            }
            if (schema.getDefault() != null) {
                param.setDefaultValue(schema.getDefault());
            }
            if (schema.get$ref() != null) {
                param.setSchemaRef(schema.get$ref());
            }
        }

        return param;
    }

    /**
     * 转换请求体
     *
     * @param requestBody OpenAPI RequestBody
     * @param openApi     OpenAPI 根实例
     * @return ApiParameter 请求体参数模型
     * @author chenglin.wu
     * @date 2026/08/16
     */
    public ApiParameter convertRequestBody(RequestBody requestBody, io.swagger.v3.oas.models.OpenAPI openApi) {
        ApiParameter param = new ApiParameter();
        param.setName("body");
        param.setIn("body");
        param.setDescription(requestBody.getDescription());
        param.setRequired(requestBody.getRequired() != null && requestBody.getRequired());

        if (requestBody.getContent() != null) {
            Map<String, Schema> allSchemas = openApi.getComponents() != null
                    ? openApi.getComponents().getSchemas() : null;
            for (Map.Entry<String, MediaType> entry : requestBody.getContent().entrySet()) {
                MediaType mediaType = entry.getValue();
                if (mediaType.getSchema() != null) {
                    Schema schema = mediaType.getSchema();
                    if (schema.get$ref() != null) {
                        param.setSchemaRef(schema.get$ref());
                    } else if ("object".equals(schema.getType())) {
                        param.setProperties(convertSchemaProperties(schema, allSchemas));
                    }
                }
            }
        }

        return param;
    }

    /**
     * 转换响应
     *
     * @param responses OpenAPI 响应
     * @param openApi   OpenAPI 根实例
     * @return List<ApiResponse> 响应列表
     * @author chenglin.wu
     * @date 2026/08/16
     */
    public List<ApiResponse> convertResponses(ApiResponses responses, io.swagger.v3.oas.models.OpenAPI openApi) {
        List<ApiResponse> result = new ArrayList<>();
        Map<String, Schema> allSchemas = openApi.getComponents() != null
                ? openApi.getComponents().getSchemas() : null;
        responses.forEach((statusCode, response) -> {
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatusCode(statusCode);
            apiResponse.setDescription(response.getDescription());

            if (response.getContent() != null) {
                for (Map.Entry<String, MediaType> entry : response.getContent().entrySet()) {
                    apiResponse.getContentTypes().add(entry.getKey());
                    MediaType mediaType = entry.getValue();
                    if (mediaType.getSchema() != null) {
                        Schema schema = mediaType.getSchema();
                        ApiParameter body = new ApiParameter();
                        if (schema.get$ref() != null) {
                            body.setSchemaRef(schema.get$ref());
                        } else if ("object".equals(schema.getType())) {
                            body.setProperties(convertSchemaProperties(schema, allSchemas));
                        } else if ("array".equals(schema.getType()) && schema instanceof ArraySchema) {
                            ArraySchema arraySchema = (ArraySchema) schema;
                            if (arraySchema.getItems() != null && arraySchema.getItems().get$ref() != null) {
                                body.setSchemaRef(arraySchema.getItems().get$ref());
                            }
                        }
                        apiResponse.setBody(body);
                    }
                }
            }

            if (response.getHeaders() != null) {
                List<ApiParameter> headers = new ArrayList<>();
                response.getHeaders().forEach((name, header) -> {
                    ApiParameter headerParam = new ApiParameter();
                    headerParam.setName(name);
                    headerParam.setIn("header");
                    if (header.getSchema() != null) {
                        Schema schema = header.getSchema();
                        headerParam.setType(schema.getType());
                        headerParam.setFormat(schema.getFormat());
                    }
                    headers.add(headerParam);
                });
                apiResponse.setHeaders(headers);
            }

            result.add(apiResponse);
        });
        return result;
    }

    /**
     * 转换模型
     *
     * @param name       模型名称
     * @param schema     OpenAPI Schema
     * @param allSchemas 所有 Schema 引用表
     * @return ApiModel 自定义模型
     * @author chenglin.wu
     * @date 2026/08/16
     */
    public ApiModel convertModel(String name, Schema schema, Map<String, Schema> allSchemas) {
        ApiModel model = new ApiModel();
        model.setName(name);
        model.setDescription(schema.getDescription());
        model.setType(schema.getType());
        model.setExample(schema.getExample());

        if (schema.getRequired() != null) {
            List<String> required = (List<String>) schema.getRequired();
            model.setRequired(new ArrayList<>(required));
        }

        if (schema.getAllOf() != null && !schema.getAllOf().isEmpty()) {
            Schema<?> parent = (Schema<?>) schema.getAllOf().get(0);
            if (parent.get$ref() != null) {
                model.setParentRef(OpenApiUtil.extractRefName(parent.get$ref()));
            }
        }

        if (schema.getDiscriminator() != null) {
            model.setDiscriminator(schema.getDiscriminator().getPropertyName());
        }

        if ("object".equals(schema.getType())) {
            model.setProperties(convertSchemaProperties(schema, allSchemas));
        }

        return model;
    }

    /**
     * 转换 Schema 属性（含嵌套 object / array / $ref）
     *
     * @param schema     OpenAPI Schema
     * @param allSchemas 所有 Schema 引用表（用于嵌套解析），允许为空
     * @return List<ApiParameter> 转换后的属性列表
     * @author chenglin.wu
     * @date 2026/08/16
     */
    public List<ApiParameter> convertSchemaProperties(Schema schema, Map<String, Schema> allSchemas) {
        List<ApiParameter> properties = new ArrayList<>();
        if (schema.getProperties() == null) {
            return properties;
        }

        Map<String, Schema> schemaProperties = schema.getProperties();
        schemaProperties.forEach((propName, propSchema) -> {
            ApiParameter param = new ApiParameter();
            param.setName(propName);
            if (propSchema != null) {
                param.setDescription(propSchema.getDescription());
                param.setType(propSchema.getType());
                param.setFormat(propSchema.getFormat());
                param.setExample(propSchema.getExample());

                if (propSchema.getEnum() != null) {
                    param.setEnumValues(new ArrayList<>(propSchema.getEnum()));
                }
                if (propSchema.getDefault() != null) {
                    param.setDefaultValue(propSchema.getDefault());
                }
                if (propSchema.get$ref() != null) {
                    param.setSchemaRef(OpenApiUtil.extractRefName(propSchema.get$ref()));
                } else if ("object".equals(propSchema.getType())) {
                    param.setProperties(convertSchemaProperties(propSchema, allSchemas));
                } else if ("array".equals(propSchema.getType()) && propSchema instanceof ArraySchema) {
                    ArraySchema arraySchema = (ArraySchema) propSchema;
                    if (arraySchema.getItems() != null) {
                        ApiParameter items = new ApiParameter();
                        items.setType(arraySchema.getItems().getType());
                        items.setFormat(arraySchema.getItems().getFormat());
                        if (arraySchema.getItems().get$ref() != null) {
                            items.setSchemaRef(OpenApiUtil.extractRefName(arraySchema.getItems().get$ref()));
                        }
                        param.setItems(items);
                    }
                }
            }

            properties.add(param);
        });

        return properties;
    }

    /**
     * 转换安全方案
     * <p>
     * 仅保留通用字段（type/description/scheme/bearerFormat/openIdConnectUrl），
     * OAuth flow 由调用方按需深度展开。
     *
     * @param name    安全方案名
     * @param scheme  OpenAPI SecurityScheme（通过 Object 引用避免强耦合 io.swagger 子包）
     * @return ApiSecurityScheme 安全方案 DTO
     * @author chenglin.wu
     * @date 2026/08/16
     */
    public com.mujin.document.model.ApiSecurityScheme convertSecurityScheme(String name, Object scheme) {
        com.mujin.document.model.ApiSecurityScheme dto = new com.mujin.document.model.ApiSecurityScheme();
        dto.setName(name);
        if (scheme == null) {
            return dto;
        }
        try {
            java.lang.reflect.Method getType = scheme.getClass().getMethod("getType");
            Object type = getType.invoke(scheme);
            if (type != null) {
                dto.setType(type.toString());
            }
        } catch (Exception ignored) {
            // 反射失败时 type 字段保留为 null
        }
        try {
            java.lang.reflect.Method getDesc = scheme.getClass().getMethod("getDescription");
            Object desc = getDesc.invoke(scheme);
            if (desc != null) {
                dto.setDescription(desc.toString());
            }
        } catch (Exception ignored) {
            // 反射失败时 description 字段保留为 null
        }
        try {
            java.lang.reflect.Method getBearer = scheme.getClass().getMethod("getBearerFormat");
            Object bearer = getBearer.invoke(scheme);
            if (bearer != null) {
                dto.setBearerFormat(bearer.toString());
            }
        } catch (Exception ignored) {
            // 反射失败时 bearerFormat 字段保留为 null
        }
        try {
            java.lang.reflect.Method getOidc = scheme.getClass().getMethod("getOpenIdConnectUrl");
            Object oidc = getOidc.invoke(scheme);
            if (oidc != null) {
                dto.setOpenIdConnectUrl(oidc.toString());
            }
        } catch (Exception ignored) {
            // 反射失败时 openIdConnectUrl 字段保留为 null
        }
        return dto;
    }

    /**
     * 从 Components 中提取安全方案列表
     *
     * @param components OpenAPI Components
     * @return List<ApiSecurityScheme> 安全方案列表；components 为空时返回空列表
     * @author chenglin.wu
     * @date 2026/08/16
     */
    public List<com.mujin.document.model.ApiSecurityScheme> convertSecuritySchemes(Components components) {
        List<com.mujin.document.model.ApiSecurityScheme> result = new ArrayList<>();
        if (components == null || components.getSecuritySchemes() == null) {
            return result;
        }
        components.getSecuritySchemes().forEach((name, scheme) -> result.add(convertSecurityScheme(name, scheme)));
        return result;
    }
}
