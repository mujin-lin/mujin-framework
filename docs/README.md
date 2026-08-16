# mujin-framework 文档索引

> 本目录按模块拆分提供架构与使用说明。每个文档聚焦该模块的设计意图、关键 API、配置项、示例与 FAQ。

## 📚 文档导航

### 全局

| 文档 | 说明 |
| --- | --- |
| [`architecture.md`](./architecture.md) | 框架整体架构：模块依赖图、插件化策略、请求处理时序、扩展点 |
| [`logging-design.md`](./logging-design.md) | `mujin-web-logging` 设计文档（早期方案） |
| [`README.md`](./README.md) | （待补充）文档目录的快速导航 |

### Commons 模块（无 Spring 依赖）

| 文档 | 模块 | 说明 |
| --- | --- | --- |
| [`commons-lang-architecture.md`](./commons-lang-architecture.md) | `commons-lang` | 时间/JSON/正则/加密/异常/错误码 |
| [`commons-web-architecture.md`](./commons-web-architecture.md) | `commons-web` | 注解/请求/响应/校验/登录用户管理 |
| [`commons-csv-architecture.md`](./commons-csv-architecture.md) | `commons-csv` | CSV 序列化与反序列化 |

### Boot Starter 模块（Spring Boot 自动装配）

| 文档 | 模块 | 说明 |
| --- | --- | --- |
| [`mujin-web-boot-starter-architecture.md`](./mujin-web-boot-starter-architecture.md) | `mujin-web-boot-starter` | 框架入口装配（默认启用） |
| [`mujin-web-cache-architecture.md`](./mujin-web-cache-architecture.md) | `mujin-web-cache` | Redis / 本地缓存 |
| [`mujin-web-orm-architecture.md`](./mujin-web-orm-architecture.md) | `mujin-web-orm` | MyBatis-Plus 增强 |
| [`mujin-web-logging-architecture.md`](./mujin-web-logging-architecture.md) | `mujin-web-logging` | 操作日志（DB/FILE/Kafka） |
| [`mujin-web-security-architecture.md`](./mujin-web-security-architecture.md) | `mujin-web-security` | 校验器链 |
| [`mujin-web-document-architecture.md`](./mujin-web-document-architecture.md) | `mujin-web-document` | 接口文档（OpenAPI + PDF） |
| [`mujin-web-model-architecture.md`](./mujin-web-model-architecture.md) | `mujin-web-model` | 通用模型（占位模块） |

### 各模块 README

| README | 模块 |
| --- | --- |
| [`../mujin-boot-starter/mujin-web-document/README.md`](../mujin-boot-starter/mujin-web-document/README.md) | `mujin-web-document` 完整使用手册 |

---

## 🎯 按角色阅读建议

### 新人入门（5 分钟）

1. 阅读项目根 [`README.md`](../README.md)
2. 浏览 [`architecture.md`](./architecture.md) — 理解模块划分与插件化策略
3. 按需跳转到具体模块的 README

### 业务接入（按模块选读）

| 我想做的事 | 推荐阅读 |
| --- | --- |
| 使用统一响应、登录校验 | [`commons-web-architecture.md`](./commons-web-architecture.md) |
| 加密、时间、正则、JSON | [`commons-lang-architecture.md`](./commons-lang-architecture.md) |
| CSV 导入导出 | [`commons-csv-architecture.md`](./commons-csv-architecture.md) |
| 启用缓存（Redis / 本地） | [`mujin-web-cache-architecture.md`](./mujin-web-cache-architecture.md) |
| MyBatis-Plus 自动填充 / 分页 | [`mujin-web-orm-architecture.md`](./mujin-web-orm-architecture.md) |
| 启用操作日志 | [`mujin-web-logging-architecture.md`](./mujin-web-logging-architecture.md) |
| 自定义登录/限流校验 | [`mujin-web-security-architecture.md`](./mujin-web-security-architecture.md) |
| 启用接口文档 / PDF 导出 | [`../mujin-boot-starter/mujin-web-document/README.md`](../mujin-boot-starter/mujin-web-document/README.md) + [`mujin-web-document-architecture.md`](./mujin-web-document-architecture.md) |

### 二次开发（架构深入）

1. [`architecture.md`](./architecture.md) — 框架全局
2. 各模块 `*-architecture.md` — 重点关注「包结构」「关键设计」「扩展点」
3. 源代码 `src/main/java` — 最终依据

---

## 🔍 关键词索引

| 关键词 | 出现位置 |
| --- | --- |
| `@OperationLog` | logging |
| `@LogMask` / `@LogIgnore` | logging |
| `@CsvProperty` | commons-csv |
| `@LoginCheck` / `@AccessLimit` | commons-web |
| `@EnableAutoFill` | orm |
| `@EnableCacheCustomizer` | cache |
| `@EnableSecurityValidator` | security |
| `ResponseResult` / `ResponseUtils` | commons-web |
| `BusinessException` / `CommonsException` | commons-lang |
| `Caffeine` | cache / document |
| `MyBatis-Plus` | orm |
| `springdoc-openapi` | document |
| `Apache PDFBox` | document |
| `Apache Commons Lang3` | commons-lang |
| `Hutool` | commons-lang / cache |
| `JWT` / `OAuth2` | commons-web / security |

---

## 📝 文档维护

- 每个新模块应同步提供 `README.md`（模块级）+ `docs/<module>-architecture.md`（架构级）
- 文档变更需随代码 PR 一起提交
- 重大架构调整需更新 [`architecture.md`](./architecture.md)
