# mujin-web-document

> 基于 **springdoc-openapi 2.7.0 + Apache PDFBox 3.x** 的接口文档增强模块。
> 提供 OpenAPI 3.0 自动生成、Swagger UI、JSON / YAML 规范导出、PDF 文档导出、分组分页与搜索。

[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)]()
[![JDK](https://img.shields.io/badge/JDK-21%2B-orange.svg)]()
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.8-brightgreen.svg)]()

---

## ✨ 核心特性

| 特性 | 说明 |
| --- | --- |
| 🪄 零配置扫描 | 自动扫描 Spring 应用上下文中所有 `@RestController` / `@RequestMapping`，第三方包（`com.jjj.xxx`）天然识别，无需配置 `packagesToScan` |
| 📚 OpenAPI 3 | 完整 OpenAPI 3.0 规范生成（info / paths / components / tags / security） |
| 🎨 Swagger UI | 自定义路径 `/doc.html`，支持主题、扩展点 |
| 📤 多格式导出 | JSON / YAML / PDF 三种格式，PDF 基于 Apache PDFBox 3.x（Apache 2.0，**无 AGPL 风险**） |
| 🧩 分组管理 | 多模块分组（`mujin.document.groups[]`），JSON/YAML/PDF 全部支持按分组过滤 |
| 🔍 分页与搜索 | `/api-docs/groups`、`/api-docs/endpoints` 均支持 `page / size / search` |
| ⚡ Caffeine 缓存 | `parseAllGroups()` 内置 Caffeine，命中后 < 5ms，TTL/容量可配 |
| 🛡️ 统一异常 | `DocumentExceptionHandler` 统一处理业务/框架/序列化/IO 异常 |
| 🧪 单元测试 | 49 个测试用例，覆盖 mapper / parser / PDF / Controller |

---

## 📦 模块坐标

```xml
<groupId>com.mujin.boot</groupId>
<artifactId>mujin-web-document</artifactId>
<version>1.0.0-SNAPSHOT</version>
<packaging>jar</packaging>
```

依赖：

- `springdoc-openapi-starter-webmvc-ui`（2.7.0）
- `org.apache.pdfbox:pdfbox`（3.0.3）
- `com.github.ben-manes.caffeine:caffeine`（由 Spring Boot BOM 统一管理）
- `com.fasterxml.jackson.dataformat:jackson-dataformat-yaml`
- `commons-lang`、`commons-web`

---

## 🚀 快速开始

### 1. 引入依赖

```xml
<dependency>
    <groupId>com.mujin.boot</groupId>
    <artifactId>mujin-web-document</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

> 框架遵循「**引入依赖 ≠ 启用功能**」的插件化原则，默认关闭，需在 `application.yml` 显式启用。

### 2. 启用配置

```yaml
mujin:
  document:
    enabled: true                       # 启用接口文档（默认 false）
    title: "业务系统 API 文档"
    version: "1.0.0"
    description: "基于 OpenAPI 3.0 自动生成"

    swagger-ui:
      path: /doc.html                   # Swagger UI 路径
      enabled: true

    pdf-export:
      enabled: true                     # 启用 PDF 导出
      engine: PDFBOX                    # PDF 生成引擎
      font-path: /opt/fonts/simhei.ttf  # 中文字体（可选）
      page-size: A4                     # A4 / LETTER
      include-examples: true
      include-models: true

    cache:                              # OpenAPI 解析缓存
      enabled: true
      ttl-seconds: 300
      max-size: 100
```

### 3. 启动验证

```bash
# 启动业务工程后访问：
#   Swagger UI
curl http://localhost:8080/doc.html

#   OpenAPI 合并规范
curl http://localhost:8080/v3/api-docs

#   分组列表
curl http://localhost:8080/api-docs/groups
```

---

## 📋 完整配置项

### 顶层配置（`mujin.document.*`）

| 配置项 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `enabled` | boolean | `false` | **总开关**，必须显式启用 |
| `title` | String | `Mujin Framework API 文档` | 文档标题 |
| `version` | String | `1.0.0` | 文档版本 |
| `description` | String | 默认描述 | 文档描述 |
| `terms-of-service-url` | String | `` | 服务条款 URL |
| `contact.{name,email,url}` | Object | `Mujin Team` | 联系人信息 |
| `license.{name,url}` | Object | `Apache 2.0` | 许可证信息 |
| `basePackages` | List | `[]` | **@Deprecated**，由 springdoc 自动扫描 |
| `excludedPaths` | List | `[]` | **@Deprecated**，由 springdoc 自动扫描 |

### Swagger UI（`mujin.document.swagger-ui.*`）

| 配置项 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `path` | String | `/doc.html` | Swagger UI 访问路径 |
| `enabled` | boolean | `true` | 是否启用 |
| `display-operation-id` | boolean | `false` | 是否显示 operationId |
| `doc-expansion` | String | `list` | 默认展开：`list` / `full` / `none` |
| `filter` | boolean | `true` | 是否启用过滤 |
| `display-request-duration` | boolean | `true` | 是否显示请求持续时间 |
| `theme` | String | `classic` | 主题：`classic` / `dark` 等 |
| `custom-css` | String | `` | 自定义 CSS 路径 |
| `custom-js` | String | `` | 自定义 JS 路径 |
| `site-title` | String | `` | 自定义站点标题 |
| `favicon` | String | `` | 自定义 favicon |

### PDF 导出（`mujin.document.pdf-export.*`）

| 配置项 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `enabled` | boolean | `false` | **PDF 导出开关** |
| `path` | String | `/api-docs/pdf` | PDF 导出接口路径 |
| `engine` | String | `PDFBOX` | PDF 引擎（当前仅 `PDFBOX`） |
| `font-path` | String | `` | 中文字体路径（绝对路径或 `classpath:`），空则降级到 Helvetica（中文显示 `?`） |
| `output-dir` | String | `target/api-docs` | 输出目录 |
| `file-name-prefix` | String | `api-document` | 文件名前缀 |
| `include-examples` | boolean | `true` | PDF 是否包含调用示例 |
| `include-models` | boolean | `true` | PDF 是否包含数据模型 |
| `page-size` | String | `A4` | 纸张大小：`A4` / `LETTER` |
| `margin` | int | `20` | 页面边距（mm） |

### 缓存（`mujin.document.cache.*`）

| 配置项 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `enabled` | boolean | `true` | 是否启用 OpenAPI 解析缓存 |
| `ttl-seconds` | int | `300` | 缓存 TTL（秒） |
| `max-size` | int | `100` | 缓存最大条目数（LRU） |

### 分组（`mujin.document.groups[]`）

```yaml
mujin:
  document:
    groups:
      - name: "user"
        displayName: "用户管理"
        packagesToScan:
          - "com.example.controller.user"
        pathsToMatch:
          - "/user/**"
        pathsToExclude:
          - "/user/internal/**"
        order: 0
```

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `name` | String | 分组名称（必填），对应 `/v3/api-docs/{name}` |
| `displayName` | String | Swagger UI 显示名称 |
| `packagesToScan` | List | 包扫描路径（可选，留空则扫描整个上下文） |
| `pathsToMatch` | List | 路径包含规则（Ant 风格） |
| `pathsToExclude` | List | 路径排除规则（Ant 风格） |
| `order` | int | 分组排序（影响 Swagger UI 顺序） |

---

## 🌐 REST 接口清单

`DocumentAutoConfiguration` 启动后自动注册以下接口（基于 `@RestController`）：

| 方法 | 路径 | 说明 | 响应 |
| --- | --- | --- | --- |
| GET | `/api-docs/groups` | 分组列表（支持 `page` / `size` / `search`） | `ResponseResult<PageResult<String>>` |
| GET | `/api-docs/spec/{group}` | 分组 OpenAPI JSON（支持 `search`） | `application/json` |
| GET | `/api-docs/export/json` | OpenAPI JSON 下载（支持 `groups` 多值过滤） | `attachment; filename=openapi.json` |
| GET | `/api-docs/export/yaml` | OpenAPI YAML 下载 | `application/yaml` |
| POST | `/api-docs/export/pdf` | PDF 导出（请求体 `ExportRequest`） | `application/pdf` |
| GET | `/api-docs/endpoints` | 端点列表（支持 `page` / `size` / `search`） | `ResponseResult<PageResult<ApiEndpoint>>` |

springdoc 默认接口（仍然生效）：

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/v3/api-docs` | 合并 OpenAPI 规范 |
| GET | `/v3/api-docs/{group}` | 分组级 OpenAPI 规范 |
| GET | `/swagger-ui.html` | springdoc 默认 Swagger UI（推荐改用 `/doc.html`） |
| GET | `/doc.html` | 本模块自定义 Swagger UI |

---

## 🚦 接口调用示例

### 1. 获取分组列表（分页 + 搜索）

```bash
# 默认参数（page=0, size=20）
curl http://localhost:8080/api-docs/groups

# 自定义分页
curl "http://localhost:8080/api-docs/groups?page=0&size=10"

# 模糊搜索
curl "http://localhost:8080/api-docs/groups?search=user"
```

响应：

```json
{
  "resCode": 0,
  "resMsg": "success",
  "resData": {
    "page": 0,
    "size": 20,
    "total": 2,
    "items": ["user", "order"]
  }
}
```

### 2. 获取指定分组的 OpenAPI

```bash
# 全量
curl http://localhost:8080/api-docs/spec/user

# 模糊搜索接口
curl "http://localhost:8080/api-docs/spec/user?search=login"
```

### 3. 下载 OpenAPI JSON / YAML

```bash
# 全量下载
curl -OJ http://localhost:8080/api-docs/export/json
curl -OJ http://localhost:8080/api-docs/export/yaml

# 按分组过滤（多个分组用逗号分隔）
curl -OJ "http://localhost:8080/api-docs/export/json?groups=user,order"
```

### 4. 导出 PDF 文档

```bash
curl -X POST http://localhost:8080/api-docs/export/pdf \
  -H "Content-Type: application/json" \
  -d '{
    "format": "PDF",
    "includeExamples": true,
    "includeModels": true,
    "includeDeprecated": false,
    "pageSize": "A4",
    "margin": 20,
    "languages": ["curl", "java", "python", "javascript"],
    "groups": ["user", "order"],
    "tags": [],
    "fileName": "my-api-doc"
  }' \
  -o my-api-doc.pdf
```

`ExportRequest` 字段说明：

| 字段 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `format` | String | `PDF` | 导出格式（当前仅 `PDF`） |
| `groups` | List | `[]` | 分组过滤（空=全量） |
| `tags` | List | `[]` | 标签过滤（空=全量） |
| `includeExamples` | boolean | `true` | 是否包含调用示例 |
| `includeModels` | boolean | `true` | 是否包含数据模型 |
| `includeDeprecated` | boolean | `false` | 是否包含已弃用接口 |
| `languages` | List | `[curl, java, python, javascript]` | 代码示例语言 |
| `pageSize` | String | `A4` | 纸张大小 |
| `margin` | int | `20` | 页面边距（mm） |
| `fileName` | String | 时间戳 | 自定义文件名（不含扩展名） |

### 5. 列出所有端点

```bash
curl "http://localhost:8080/api-docs/endpoints?page=0&size=50&search=user"
```

---

## 🈶 中文字体配置

PDF 默认使用 PDFBox 内置 Helvetica，**不支持中文**。需提供 TTF 字体：

```yaml
mujin:
  document:
    pdf-export:
      # 三种方式任选其一
      font-path: /usr/share/fonts/simhei.ttf        # Linux 绝对路径
      font-path: C:/Windows/Fonts/msyh.ttc          # Windows 绝对路径
      font-path: classpath:fonts/simhei.ttf         # classpath 资源
```

字体未找到时自动降级到 Helvetica（中文显示 `?`），不会抛出异常。生产环境**务必**配置。

---

## 🧪 测试

模块内置 **49 个测试用例**（覆盖率 ≥ 80%）：

| 测试类 | 用例数 | 覆盖内容 |
| --- | --- | --- |
| `OpenApiModelMapperTest` | 11 | Tag/Parameter/RequestBody/Responses/Model/Schema/SecurityScheme |
| `OpenApiPathParserTest` | 7 | HTTP 方法遍历、分组推断、deprecated |
| `OpenApiParserServiceTest` | 7 | 基本解析、缓存命中/失效、null 保护 |
| `PdfBoxLayoutEngineTest` | 7 | 页面尺寸、Y 坐标、换页判断 |
| `PdfBoxTableRendererTest` | 5 | 列宽计算、文本截断、表头绘制 |
| `PdfBoxFontRegistryTest` | 4 | 默认加载、TTF 失败降级 |
| `DocumentControllerTest` | 8 | 分组查询、参数校验、JSON 导出、端点列表 |

```bash
# 运行测试
mvn -pl mujin-boot-starter/mujin-web-document test
```

---

## ❓ 常见问题

**Q1：引入依赖后 `/v3/api-docs` 是空的？**  
需在 `application.yml` 设置 `mujin.document.enabled=true`。模块默认关闭——这是插件化原则。

**Q2：第三方项目（`com.jjj.xxx`）的 Controller 会被自动扫描到吗？**  
会。springdoc 2.7 默认扫描整个 Spring 应用上下文，`mujin-web-document` 完全复用其行为，无需配置 `packagesToScan`。

**Q3：PDF 导出时中文显示 `?`？**  
PDFBox 内置 Helvetica 不支持中文。需在 `mujin.document.pdf-export.font-path` 配置 TTF 字体路径。

**Q4：为什么用 Apache PDFBox 而非 iText 7？**  
iText 7 自 2021 年起改用 AGPL v3 许可证，强传染协议——任何"通过网络向用户提供"的应用（即便内网）必须开源。`mujin-framework` 作为被多个业务项目以二进制形式引用的基础框架，禁止引入此类依赖。PDFBox 3.x 是 Apache License 2.0，可商用、无传染性。

**Q5：缓存 TTL 怎么调？**  
通过 `mujin.document.cache.ttl-seconds`（默认 300 秒）。配置变更后通过 `OpenApiParserService#invalidateCache()` 主动清空。

**Q6：分组 `groups[]` 不配置会怎样？**  
springdoc 自动使用默认 `default` 分组，扫描整个上下文。第三方包同样会被识别。配置 `groups[]` 后才会生成 `GroupedOpenApi` Bean。

**Q7：PDF 中表格跨页时表头会重复吗？**  
是。`PdfBoxTableRenderer` 在表格超出当前页时自动新建页并重绘表头。

---

## 📂 包结构

```
com.mujin.document/
├── auto/                    DocumentAutoConfiguration
├── code/                    DocumentErrorCode（6001-6099）
├── configuration/           DocumentProperties（mujin.document.*）
├── controller/              DocumentController（/api-docs/*）
├── handler/                 DocumentExceptionHandler（统一异常处理）
├── model/                   ApiDocument / ApiEndpoint / PageResult / ...
├── service/
│   ├── OpenApiParserService.java   # 编排层（含 Caffeine 缓存）
│   ├── OpenApiModelMapper.java     # DTO 映射
│   ├── OpenApiPathParser.java      # PathItem → ApiEndpoint
│   ├── PdfExportService.java       # PDF 导出接口
│   ├── CodeExampleGenerator.java   # 代码示例生成
│   └── impl/
│       └── PdfBoxPdfExportService.java
└── util/
    ├── OpenApiUtil.java            # $ref 提取 / OpenAPI 合并
    ├── PdfBoxTextUtils.java        # 文本工具
    └── pdf/
        ├── PdfBoxFontRegistry.java # 字体加载（含降级）
        ├── PdfBoxLayoutEngine.java # 布局状态
        └── PdfBoxTableRenderer.java # 表格渲染（跨页表头）
```

---

## 🔗 相关文档

- [`docs/mujin-web-document-architecture.md`](../../docs/mujin-web-document-architecture.md) — 架构与设计详解
- [`docs/architecture.md`](../../docs/architecture.md) — 框架整体架构
- [springdoc-openapi 官方文档](https://springdoc.org/)
- [Apache PDFBox 官方文档](https://pdfbox.apache.org/)
