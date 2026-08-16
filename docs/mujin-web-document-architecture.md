# mujin-web-document 架构与设计

> 本文档面向希望深入了解 mujin-web-document 模块内部实现的开发者。

## 1. 模块定位

`mujin-web-document` 是基于 **Spring Boot 3.5.x + springdoc-openapi 2.7 + Apache PDFBox 3.x** 的接口文档增强模块：

- 通过 springdoc 自动扫描 `@RestController` 生成 OpenAPI 3.0 规范
- 提供 Swagger UI 自定义页面（`/doc.html`）
- 支持多模块分组（仅在显式配置时生效）
- 提供 PDF 导出能力（基于 Apache PDFBox，无需任何 AGPL 依赖）
- 提供 JSON / YAML 导出能力

## 2. 包结构

```
com.mujin.document/
├── auto/                    # 自动装配（DocumentAutoConfiguration）
├── code/                    # 错误码（DocumentErrorCode）
├── configuration/           # 配置属性（DocumentProperties）
├── controller/              # HTTP 接口（DocumentController）
├── handler/                 # 异常处理（DocumentExceptionHandler）
├── model/                   # 数据模型（ApiDocument / ApiEndpoint / ...）
├── service/
│   ├── OpenApiParserService.java   # 编排层（含 Caffeine 缓存）
│   ├── OpenApiModelMapper.java     # DTO 映射（Schema → ApiModel 等）
│   ├── OpenApiPathParser.java      # PathItem → ApiEndpoint 解析
│   ├── PdfExportService.java       # PDF 导出接口
│   ├── CodeExampleGenerator.java   # 代码示例生成
│   └── impl/
│       └── PdfBoxPdfExportService.java  # PDFBox 3.x 实现
└── util/
    ├── OpenApiUtil.java            # $ref 提取 / OpenAPI 合并
    ├── PdfBoxTextUtils.java        # 文本绘制 / 清洗 / 截断
    └── pdf/
        ├── PdfBoxFontRegistry.java # 字体加载
        ├── PdfBoxLayoutEngine.java # 布局状态
        └── PdfBoxTableRenderer.java # 表格渲染（含跨页表头）
```

## 3. 核心设计

### 3.1 OpenAPI 解析三层分离

```
+---------------------------+
|   OpenApiParserService    |  编排层（缓存 + 串联）
+---------------------------+
            ↓ 使用
+-------------------+    +---------------------+
| OpenApiPathParser | →  | OpenApiModelMapper  |  DTO 映射
+-------------------+    +---------------------+
```

- **`OpenApiParserService`**：仅做编排（tags / paths / components → `ApiDocument`），
  内置 Caffeine 缓存，避免每次 HTTP 请求都重新遍历 OpenAPI。
- **`OpenApiPathParser`**：负责单个 `PathItem` 的 HTTP 方法遍历、参数合并、请求体/响应委托。
- **`OpenApiModelMapper`**：纯 DTO 映射，集中处理 `getEnum/getRequired/getAllOf` 等需要强转的 API。

### 3.2 PDFBox 渲染职责拆分

```
+------------------------------+
|   PdfBoxPdfExportService     |  顶层编排（封面/目录/章节/模型）
+------------------------------+
       ↓          ↓          ↓
+-----------+  +-----------+  +-----------------+
| FontReg   |  | LayoutEng |  | TableRenderer   |
+-----------+  +-----------+  +-----------------+
                                    ↓
                              +-------------+
                              | TextUtils   |
                              +-------------+
```

- **`PdfBoxFontRegistry`**：加载 TTF 字体或降级到 PDFBox 内置 Helvetica；暴露 regular/bold/mono。
- **`PdfBoxLayoutEngine`**：维护页面尺寸、边距、当前 Y 坐标；提供 `newPage()` / `advanceY()` / `needsNewPage()`。
- **`PdfBoxTableRenderer`**：绘制表头 + 数据行；列宽按比例自适应；调用方负责跨页切流。
- **`PdfBoxTextUtils`**：纯文本工具（drawText / sanitize / truncate / stringWidth）。

### 3.3 缓存策略

- 缓存库：Caffeine（由 Spring Boot 3.5 BOM 统一管理版本）
- 缓存粒度：`OpenApiParserService.parseAllGroups()` 整体结果
- 缓存 key：`OpenAPI.hashCode() * 31 + groupedOpenApis.size()`
- 缓存配置：`mujin.document.cache.{enabled, ttl-seconds, max-size}`
- TTL 默认 300 秒；容量上限默认 100（LRU）
- 提供 `invalidateCache()` 方法供配置热更新时手动清空

### 3.4 统一异常处理

- `DocumentExceptionHandler` 通过 `@RestControllerAdvice(basePackages = "com.mujin.document.controller")`
  拦截本模块所有 Controller 的异常
- 复用 `commons-web` 的 `ResponseUtils.fail(errCode, errMsg)` 构造错误响应
- 错误码定义在 `DocumentErrorCode`（6001-6099 段位）

## 4. 扩展点

### 4.1 新增 PDF 引擎

实现 `PdfExportService` 接口，在 `DocumentAutoConfiguration` 中注册：

```java
@Bean
@ConditionalOnMissingBean
@ConditionalOnProperty(prefix = "mujin.document.pdf-export", name = "engine", havingValue = "ITEXT")
public PdfExportService iTextPdfExportService() {
    return new ITextPdfExportService(properties);
}
```

### 4.2 新增代码示例语言

扩展 `CodeExampleGenerator`，新增 `generateXxx()` 方法，并在 `generateAll()` 中调用。

### 4.3 自定义 OpenAPI 过滤

继承 `OpenApiParserService`，重写 `parseAllGroups()`，或通过 `OpenApiPathParser` 注入自定义过滤逻辑。

## 5. 关键依赖

| 依赖 | 用途 | 许可证 |
|---|---|---|
| `springdoc-openapi-starter-webmvc-ui` | OpenAPI 3 生成 + Swagger UI | Apache 2.0 |
| `pdfbox` | PDF 渲染 | Apache 2.0 |
| `caffeine` | 解析结果缓存 | Apache 2.0 |
| `jackson-dataformat-yaml` | YAML 导出 | Apache 2.0 |
| `commons-lang` | 异常基类、错误码 | Apache 2.0 |
| `commons-web` | ResponseResult / ResponseUtils | 项目自有 |

## 6. 配置示例

```yaml
mujin:
  document:
    enabled: true
    title: "MyApp API 文档"
    version: "1.0.0"
    pdf-export:
      enabled: true
      engine: PDFBOX
      font-path: "C:/Windows/Fonts/msyh.ttc"  # 中文字体
      page-size: A4
      margin: 20
    cache:
      enabled: true
      ttl-seconds: 300
      max-size: 100
    groups:
      - name: "user"
        displayName: "用户管理"
        packagesToScan: ["com.example.controller.user"]
```

## 7. API 列表

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api-docs/groups` | 分组列表（支持分页/搜索） |
| GET | `/api-docs/spec/{group}` | 分组 OpenAPI JSON（支持搜索） |
| GET | `/api-docs/export/json` | OpenAPI JSON（支持按分组过滤） |
| GET | `/api-docs/export/yaml` | OpenAPI YAML（支持按分组过滤） |
| POST | `/api-docs/export/pdf` | PDF 导出（支持分组/标签过滤） |
| GET | `/api-docs/endpoints` | 端点列表（支持分页/搜索） |
| GET | `/doc.html` | Swagger UI 自定义页面 |

## 8. 测试覆盖

- `OpenApiModelMapperTest`：11 个用例，覆盖 Tag/Parameter/RequestBody/Responses/Model/Schema/SecurityScheme
- `OpenApiPathParserTest`：7 个用例，覆盖 HTTP 方法遍历、分组推断、deprecated
- `PdfBoxLayoutEngineTest`：7 个用例，覆盖页面尺寸、Y 坐标、换页判断
- `PdfBoxTableRendererTest`：5 个用例，覆盖列宽计算、文本截断
- `PdfBoxFontRegistryTest`：4 个用例，覆盖默认加载、TTF 加载失败降级
- `OpenApiParserServiceTest`：7 个用例，覆盖基本解析、缓存命中/失效
- `DocumentControllerTest`：8 个用例，覆盖分组查询、参数校验、JSON 导出、端点列表
