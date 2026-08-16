package com.mujin.document.service;

import com.mujin.document.model.ApiEndpoint;
import com.mujin.document.model.ApiParameter;
import com.mujin.document.model.CodeExample;

import java.util.ArrayList;
import java.util.List;

/**
 * API 调用代码示例生成器
 * <p>
 * 根据 {@link ApiEndpoint} 模型生成 cURL、Java（HttpClient）、Python（requests）、
 * JavaScript（fetch）四种语言的调用示例，供前端调试面板与 PDF 导出复用。
 *
 * @author chenglin.wu
 * @date 2026/08/16
 */
@SuppressWarnings("unused")
public class CodeExampleGenerator {

    /**
     * 生成所有支持语言的代码示例
     *
     * @param endpoint API 端点模型
     * @param baseUrl  接口基础地址（如 {@code http://localhost:8080}）
     * @return List<CodeExample> 代码示例列表
     * @author chenglin.wu
     * @date 2026/08/16
     */
    public List<CodeExample> generateAll(ApiEndpoint endpoint, String baseUrl) {
        List<CodeExample> examples = new ArrayList<>();
        examples.add(generateCurl(endpoint, baseUrl));
        examples.add(generateJava(endpoint, baseUrl));
        examples.add(generatePython(endpoint, baseUrl));
        examples.add(generateJavaScript(endpoint, baseUrl));
        return examples;
    }

    /**
     * 生成 cURL 调用示例
     *
     * @param endpoint API 端点模型
     * @param baseUrl  接口基础地址
     * @return CodeExample cURL 示例
     * @author chenglin.wu
     * @date 2026/08/16
     */
    public CodeExample generateCurl(ApiEndpoint endpoint, String baseUrl) {
        StringBuilder sb = new StringBuilder();
        sb.append("curl -X ").append(endpoint.getMethod().toUpperCase()).append(" \\\n");
        sb.append("  \"").append(buildUrl(endpoint, baseUrl)).append("\"");

        if (endpoint.getParameters() != null) {
            for (ApiParameter param : endpoint.getParameters()) {
                if ("header".equalsIgnoreCase(param.getIn())) {
                    sb.append(" \\\n  -H \"").append(param.getName()).append(": <value>\"");
                }
            }
        }

        if (endpoint.getRequestBody() != null) {
            sb.append(" \\\n  -H \"Content-Type: application/json\"");
            sb.append(" \\\n  -d '{ \"<your-body>\" }'");
        }

        CodeExample example = new CodeExample();
        example.setLanguage("curl");
        example.setTitle("cURL");
        example.setCode(sb.toString());
        example.setDescription("使用 cURL 命令行调用接口");
        return example;
    }

    /**
     * 生成 Java（HttpClient）调用示例
     *
     * @param endpoint API 端点模型
     * @param baseUrl  接口基础地址
     * @return CodeExample Java 示例
     * @author chenglin.wu
     * @date 2026/08/16
     */
    public CodeExample generateJava(ApiEndpoint endpoint, String baseUrl) {
        StringBuilder sb = new StringBuilder();
        sb.append("HttpClient client = HttpClient.newHttpClient();\n");
        sb.append("HttpRequest request = HttpRequest.newBuilder()\n");
        sb.append("        .uri(URI.create(\"").append(buildUrl(endpoint, baseUrl)).append("\"))\n");
        sb.append("        .method(\"").append(endpoint.getMethod().toUpperCase())
                .append("\", HttpRequest.BodyPublishers.noBody())\n");
        sb.append("        .build();\n");
        sb.append("HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());\n");
        sb.append("System.out.println(response.body());");

        CodeExample example = new CodeExample();
        example.setLanguage("java");
        example.setTitle("Java (HttpClient)");
        example.setCode(sb.toString());
        example.setDescription("使用 JDK 11+ 自带 HttpClient 调用接口");
        return example;
    }

    /**
     * 生成 Python（requests）调用示例
     *
     * @param endpoint API 端点模型
     * @param baseUrl  接口基础地址
     * @return CodeExample Python 示例
     * @author chenglin.wu
     * @date 2026/08/16
     */
    public CodeExample generatePython(ApiEndpoint endpoint, String baseUrl) {
        StringBuilder sb = new StringBuilder();
        sb.append("import requests\n\n");
        sb.append("response = requests.request(\n");
        sb.append("    \"").append(endpoint.getMethod().toUpperCase()).append("\",\n");
        sb.append("    \"").append(buildUrl(endpoint, baseUrl)).append("\",\n");
        sb.append("    headers={\"Accept\": \"application/json\"}\n");
        sb.append(")\n");
        sb.append("print(response.json())");

        CodeExample example = new CodeExample();
        example.setLanguage("python");
        example.setTitle("Python (requests)");
        example.setCode(sb.toString());
        example.setDescription("使用 requests 库调用接口");
        return example;
    }

    /**
     * 生成 JavaScript（fetch）调用示例
     *
     * @param endpoint API 端点模型
     * @param baseUrl  接口基础地址
     * @return CodeExample JavaScript 示例
     * @author chenglin.wu
     * @date 2026/08/16
     */
    public CodeExample generateJavaScript(ApiEndpoint endpoint, String baseUrl) {
        StringBuilder sb = new StringBuilder();
        sb.append("fetch(\"").append(buildUrl(endpoint, baseUrl)).append("\", {\n");
        sb.append("  method: \"").append(endpoint.getMethod().toUpperCase()).append("\",\n");
        sb.append("  headers: { \"Accept\": \"application/json\" }\n");
        sb.append("}).then(response => response.json())\n");
        sb.append("  .then(data => console.log(data));\n");

        CodeExample example = new CodeExample();
        example.setLanguage("javascript");
        example.setTitle("JavaScript (fetch)");
        example.setCode(sb.toString());
        example.setDescription("使用浏览器原生 fetch 调用接口");
        return example;
    }

    /**
     * 构造完整 URL：用 path 中的占位符（如 {@code {id}}）替换为示例值
     *
     * @param endpoint API 端点模型
     * @param baseUrl  接口基础地址
     * @return String 完整 URL
     * @author chenglin.wu
     * @date 2026/08/16
     */
    private String buildUrl(ApiEndpoint endpoint, String baseUrl) {
        String path = endpoint.getPath() == null ? "" : endpoint.getPath();
        String prefix = baseUrl == null ? "" : baseUrl;
        if (!prefix.isEmpty() && prefix.endsWith("/") && path.startsWith("/")) {
            prefix = prefix.substring(0, prefix.length() - 1);
        }
        return prefix + path;
    }
}
