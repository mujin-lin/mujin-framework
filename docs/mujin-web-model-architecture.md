# mujin-web-model 架构与使用说明

> **占位模块**，当前为预留承载跨 starter 共享的 DTO/VO/分页模型等基础类型。
> 当前 `ResponseResult<T>` / `PageResult<T>` 等基础类型分布在各自的业务模块中，
> 未来可能迁移至本模块。

---

## 1. 当前状态

- ✅ **POM 已声明**：`com.mujin.boot:mujin-web-model:1.0.0-SNAPSHOT`
- ❌ **暂无 Java 源码**
- 📦 **依赖**：`spring-boot-starter-web`（optional）、`lombok`（optional）
- 🎯 **目标**：承载跨 starter 共享的 DTO / VO / 分页模型

---

## 2. 设计意图

随着框架各 starter 模块的成熟，部分模型类（如 `PageResult<T>`、`BaseDTO`）在多个模块中被重复定义。`mujin-web-model` 的目标是：

1. **统一基础模型** —— `PageResult<T>` / `BaseDTO` / `BaseVO` / `BaseQuery`
2. **统一分页响应** —— `ResponseResult<PageResult<T>>` 的标准化封装
3. **统一异常响应** —— `ResponseResult<Void>` 的标准错误格式
4. **跨模块复用** —— 避免每个 starter 各自重复定义

---

## 3. 迁移路线图

| 模型 | 当前位置 | 目标位置 |
| --- | --- | --- |
| `ResponseResult<T>` | `commons-web.response` | 保持不变（已在 commons） |
| `PageResult<T>` | `mujin-web-document.model` | `mujin-web-model.dto` |
| `BaseEntity<ID>` | `mujin-web-orm.entity` | 保持不变（ORM 专属） |
| `BaseErrorCode` 等枚举 | `commons-lang.code` | 保持不变（已在 commons） |

> 迁移原则：仅迁移**跨 starter 共享**的纯模型，不包含任何业务/框架特有逻辑。

---

## 4. 引入依赖

```xml
<dependency>
    <groupId>com.mujin.boot</groupId>
    <artifactId>mujin-web-model</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

> 当前模块为占位，**暂无实际 API 可使用**。后续版本会逐步加入基础模型。

---

## 5. 关键 API

> 当前**暂无 API**。一旦基础模型迁移完成，将在 README 与本架构文档同步更新。

---

## 6. 相关模块

- [`commons-web-architecture.md`](./commons-web-architecture.md) — 当前 `ResponseResult<T>` 的位置
- [`commons-lang-architecture.md`](./commons-lang-architecture.md) — 当前 `BaseErrorCode` 等枚举的位置
- [`mujin-web-orm-architecture.md`](./mujin-web-orm-architecture.md) — `BaseEntity` 当前位置

---

## 7. 常见问题

**Q1：现在有什么 API？**  
暂无。本模块当前为占位状态。

**Q2：什么时候会有实际 API？**  
待框架 v1.1+ 版本，根据各 starter 的实际需求逐步迁移。

**Q3：业务项目需要引入吗？**  
当前**无需**引入。如后续版本引入基础模型且业务需要使用，可选择性引入。
