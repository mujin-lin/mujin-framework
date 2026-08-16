# CHANGELOG

> 本项目所有用户可见的变更记录遵循 [Keep a Changelog 1.1.0](https://keepachangelog.com/zh-CN/1.1.0/) 规范。
> 版本号遵循 [Semantic Versioning](https://semver.org/lang/zh-CN/)。

## [未发布] - Unreleased

### Added（新增）
- **`mujin-web-document`**：基于 springdoc-openapi 2.7.0 的接口文档模块，支持 OpenAPI 3.0 规范自动生成、Swagger UI、JSON / YAML 规范导出、PDF 文档导出（基于 Apache PDFBox 3.x）。
- **`mujin-web-logging-db`**：操作日志的 MySQL / MyBatis-Plus 存储后端（自动建表）。
- **`mujin-web-logging-kafka`**：操作日志的 Kafka 存储后端。
- **`mujin-web-model`**：通用响应模型 `ResponseResult<T>` 的占位模块。

### Changed（变更）
- **`mujin-web-document`**：
  - PDF 生成引擎从 iText 7 切换到 **Apache PDFBox 3.0.3**（Apache License 2.0，可商用）。
  - 包扫描策略：删除硬编码的 `packagesToScan = ["com.mujin"]`，完全依赖 springdoc 2.7 默认行为——扫描整个 Spring 应用上下文中所有 `@RestController` / `@RequestMapping`，第三方包（如 `com.jjj.xxx`）的 Controller 自动被识别。
  - 默认 PDF 引擎：`PDFBOX`；可通过 `mujin.document.pdf-export.font-path` 配置中文字体（TTF/OTF）。
- **`mujin-web-logging`**：`enabled` 字段默认改为 `false`，需显式 `mujin.logging.enabled=true` 启用（插件化原则）。
- **`mujin-web-cache`**：`CacheManagerAutoConfiguration` 补全 `@Configuration` 注解，新增 `mujin.cache.enabled` 开关（默认 `false`）。
- **`mujin-web-orm`**：`OrmAutoConfiguration` 补全 `@Configuration` 注解，新增 `mujin.orm.enabled` 开关（默认 `false`）。
- **父 POM**：移除 iText7 依赖管理（kernel / layout / io / commons），新增 PDFBox 3.0.3（pdfbox + fontbox）+ springdoc-openapi-starter-webmvc-api 的版本管理。

### Removed（移除）
- **`mujin-web-document`**：删除 `ITextPdfExportService`（iText 7 自 2021 起改用 AGPL v3 许可证，强传染协议，禁止用于商业闭源产品）。
- **`mujin-web-document/pom.xml`**：移除 `itext7-core` 隐式依赖。

### Security（安全）
- 拒绝任何 AGPL / GPL 强传染协议依赖进入框架（iText 7 已替换为 PDFBox 3.x）。

### Migration（迁移指南）

#### 启用插件化模块
本轮变更后，**所有可选模块默认关闭**。如需启用，需在 `application.yml` 显式配置：

```yaml
mujin:
  document:
    enabled: true                    # 接口文档
    pdf-export:
      enabled: true                  # PDF 导出（依赖 mujin.document.enabled）
      font-path: /opt/fonts/simhei.ttf  # 中文字体（可选）
  logging:
    enabled: true                    # 操作日志
  cache:
    enabled: true                    # 缓存
  orm:
    enabled: true                    # ORM 增强
```

#### 中文字体
如 PDF 中需要显示中文，下载 TTF 字体文件（如思源黑体 / 微软雅黑）并在配置中指定路径：

```yaml
mujin:
  document:
    pdf-export:
      font-path: classpath:fonts/simhei.ttf   # 或绝对路径
```

未配置时降级到 PDFBox 内置 Helvetica，中文会显示为 `?`。

---

## [1.0.0-SNAPSHOT] - 2026-08-16

### Added
- 初始快照版本：完成 `commons-lang` / `commons-csv` / `commons-web` 三大公共模块与 7 个 starter 模块。
- 父 POM 统一依赖版本、Checkstyle 规则、Lombok 1.18.42 注解处理器配置。

### Notes
- 此版本不保证 API 稳定（SNAPSHOT），后续版本可能引入破坏性变更。
