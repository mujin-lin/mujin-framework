package com.mujin.document.service;

import com.mujin.document.model.ApiEndpoint;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.parameters.Parameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * OpenAPI PathItem → ApiEndpoint 解析器
 * <p>
 * 仅负责单个 PathItem 内的 HTTP 方法遍历、参数合并、请求体/响应委托，
 * DTO 映射交由 {@link OpenApiModelMapper} 完成。
 *
 * @author chenglin.wu
 * @date 2026/08/16
 */
public class OpenApiPathParser {

    /**
     * DTO 映射器
     */
    private final OpenApiModelMapper modelMapper;

    public OpenApiPathParser(OpenApiModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    /**
     * 解析单个路径，产出所有 HTTP 方法对应的端点
     *
     * @param path     OpenAPI 路径
     * @param pathItem OpenAPI 路径项
     * @param openApi  OpenAPI 根实例（用于嵌套 $ref 解析）
     * @return List<ApiEndpoint> 端点列表
     * @author chenglin.wu
     * @date 2026/08/16
     */
    public List<ApiEndpoint> parsePath(String path, PathItem pathItem, OpenAPI openApi) {
        List<ApiEndpoint> endpoints = new ArrayList<>();
        Map<PathItem.HttpMethod, Operation> operationMap = pathItem.readOperationsMap();
        if (operationMap == null) {
            return endpoints;
        }

        for (Map.Entry<PathItem.HttpMethod, Operation> entry : operationMap.entrySet()) {
            PathItem.HttpMethod method = entry.getKey();
            Operation operation = entry.getValue();
            if (operation == null) {
                continue;
            }

            ApiEndpoint endpoint = buildEndpoint(path, method, operation, pathItem, openApi);
            endpoints.add(endpoint);
        }

        return endpoints;
    }

    /**
     * 构造单个 ApiEndpoint
     *
     * @param path      OpenAPI 路径
     * @param method    HTTP 方法
     * @param operation 当前 operation
     * @param pathItem  路径项（用于 path 级参数）
     * @param openApi   OpenAPI 根实例
     * @return ApiEndpoint 端点
     * @author chenglin.wu
     * @date 2026/08/16
     */
    private ApiEndpoint buildEndpoint(String path,
                                      PathItem.HttpMethod method,
                                      Operation operation,
                                      PathItem pathItem,
                                      OpenAPI openApi) {
        ApiEndpoint endpoint = new ApiEndpoint();
        endpoint.setMethod(method.name());
        endpoint.setPath(path);
        endpoint.setSummary(operation.getSummary());
        endpoint.setDescription(operation.getDescription());
        endpoint.setOperationId(operation.getOperationId());
        endpoint.setDeprecated(operation.getDeprecated() != null && operation.getDeprecated());

        if (operation.getTags() != null) {
            endpoint.setTags(new ArrayList<>(operation.getTags()));
        }

        endpoint.setGroup(inferGroup(operation));

        // path 级参数 + operation 级参数（去重由调用方控制）
        if (pathItem.getParameters() != null) {
            for (Parameter param : pathItem.getParameters()) {
                endpoint.getParameters().add(modelMapper.convertParameter(param));
            }
        }
        if (operation.getParameters() != null) {
            for (Parameter param : operation.getParameters()) {
                endpoint.getParameters().add(modelMapper.convertParameter(param));
            }
        }

        if (operation.getRequestBody() != null) {
            endpoint.setRequestBody(modelMapper.convertRequestBody(operation.getRequestBody(), openApi));
        }

        if (operation.getResponses() != null) {
            endpoint.setResponses(modelMapper.convertResponses(operation.getResponses(), openApi));
        }

        if (operation.getSecurity() != null) {
            List<String> security = new ArrayList<>();
            operation.getSecurity().forEach(s -> security.addAll(s.keySet()));
            endpoint.setSecurity(security);
        }

        return endpoint;
    }

    /**
     * 推断端点所属分组
     *
     * @param operation OpenAPI Operation
     * @return String 推断的分组名称
     * @author chenglin.wu
     * @date 2026/08/16
     */
    private String inferGroup(Operation operation) {
        if (operation.getOperationId() != null && operation.getOperationId().contains("_")) {
            return operation.getOperationId().split("_")[0];
        }
        if (operation.getTags() != null && !operation.getTags().isEmpty()) {
            return operation.getTags().get(0).toLowerCase();
        }
        return "default";
    }
}
