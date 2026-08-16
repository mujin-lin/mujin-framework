# mujin-web-boot-starter 架构与使用说明

> 框架入口装配模块（**默认启用**），绑定三套 `@ConfigurationProperties`：
> `CommonsProperties` / `CorsConfigProperties` / `RequestInfoPrintProperties`。

---

## 1. 模块定位

`mujin-web-boot-starter` 是整个框架的「入口」：

1. **三套配置属性** —— 加密管理器 / CORS / 请求日志打印
2. **无业务逻辑** —— 仅装配 `commons-web` 中的 `ConfigurationProperties` Bean
3. **默认启用** —— 引入依赖即生效，无需 `enabled=true`
4. **被其他 starter 依赖** —— 所有 `mujin-web-*` 模块均依赖此模块

---

## 2. 包结构

```
com.mujin.boot.web/
├── MujinWebAutoConfiguration.java
└── constants/           ConfigurationKeyConstants
```

> 配置属性类（`CommonsProperties` / `CorsConfigProperties` / `RequestInfoPrintProperties`）位于 `commons-web` 模块。

---

## 3. 启用方式

### 3.1 引入依赖

```xml
<dependency>
    <groupId>com.mujin.boot</groupId>
    <artifactId>mujin-web-boot-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

> 该模块是框架的「基础」，被所有其他 starter 模块传递依赖，**无需**显式引入。

### 3.2 配置示例

```yaml
mujin:
  web:
    config:
      commons:
        encrypt-manager-type: DEFAULT          # DEFAULT / REDIS
        login-user-manager-type: DEFAULT       # DEFAULT / REDIS
      cors:
        enable-cors: true
        mapping-path-pattern: /**
        allowed-origin-patterns:
          - "*"
        allowed-headers:
          - Authorization
          - Content-Type
        allowed-methods:
          - GET
          - POST
          - PUT
          - DELETE
          - OPTIONS
        max-age: 1800000
        allow-credentials: true
      request:
        print-uri: true
        print-request-ip: true
        print-request-os: true
        print-request-source: true
        print-request-browser: true
        print-request-param: true
        print-request-body: true
```

---

## 4. 三套配置详解

### 4.1 `CommonsProperties`（`mujin.web.config.commons`）

| 字段 | 类型 | 默认 | 说明 |
| --- | --- | --- | --- |
| `encryptManagerType` | `CommonsConfigEnum` | `DEFAULT` | 加密管理器类型（DEFAULT / REDIS） |
| `loginUserManagerType` | `CommonsConfigEnum` | `DEFAULT` | 登录用户管理器类型（DEFAULT / REDIS） |

> 这两个字段是「**类型选择器**」，告诉框架应该使用哪一套管理器实现。实际管理器 Bean 由 `mujin-web-cache`（RedisLoginUserManager）等模块提供。

### 4.2 `CorsConfigProperties`（`mujin.web.config.cors`）

详见 [`commons-web-architecture.md`](./commons-web-architecture.md) §8.2。

### 4.3 `RequestInfoPrintProperties`（`mujin.web.config.request`）

| 字段 | 默认 | 说明 |
| --- | --- | --- |
| `printUri` | false | 是否打印 URI |
| `printRequestIp` | false | 是否打印客户端 IP |
| `printRequestOs` | false | 是否打印操作系统 |
| `printRequestSource` | false | 是否打印请求来源设备 |
| `printRequestBrowser` | false | 是否打印浏览器 |
| `printRequestParam` | false | 是否打印请求参数 |
| `printRequestBody` | false | 是否打印请求 body |

> 这套配置控制**请求日志打印**的粒度。**生产环境**建议关闭 `printRequestBody`（避免敏感信息泄露），仅在调试时开启。

---

## 5. 关键 API

| 类型 | 路径 | 说明 |
| --- | --- | --- |
| 自动装配 | `com.mujin.boot.web.MujinWebAutoConfiguration` | 三套配置的自动装配 |
| 常量 | `com.mujin.boot.web.constants.ConfigurationKeyConstants` | 配置前缀常量 |
| 配置 | `com.mujin.commons.web.configuration.CommonsProperties` | Commons 配置 |
| 配置 | `com.mujin.commons.web.configuration.CorsConfigProperties` | CORS 配置 |
| 配置 | `com.mujin.commons.web.configuration.RequestInfoPrintProperties` | 请求日志配置 |

---

## 6. 与其他模块的关系

```
mujin-web-boot-starter
  ├── 依赖 commons-web（提供 ConfigurationProperties 类）
  └── 被以下模块依赖（传递依赖）
       ├── mujin-web-security
       ├── mujin-web-cache
       ├── mujin-web-orm
       ├── mujin-web-logging
       ├── mujin-web-document
       └── mujin-web-model
```

> **任何业务项目**只要引入 `mujin-web-boot-starter`，就会自动获得三套 `@ConfigurationProperties` 的支持。

---

## 7. 常见问题

**Q1：模块默认开启吗？**  
是的。`mujin-web-boot-starter` **没有** `@ConditionalOnProperty`，引入依赖即生效。

**Q2：能关闭某个配置吗？**  
三套配置都是可选的，**不配置等于关闭**。例如 `enable-cors=false` 即关闭 CORS。

**Q3：能自定义 CORS 逻辑吗？**  
可以。声明一个 `WebMvcConfigurer` 自定义 `addCorsMappings` 即可，框架的默认 CORS 配置通过 `@ConditionalOnMissingBean` 让位。

**Q4：请求日志打印输出到哪里？**  
默认通过 SLF4J 输出到 Logback，业务方可在 `logback-spring.xml` 中配置 appender。

**Q5：`encrypt-manager-type: REDIS` 但没引入 `mujin-web-cache` 怎么办？**  
框架会回退到 `DEFAULT`（无加密缓存），并在启动日志 `WARN` 提示。建议同步引入 `mujin-web-cache`。
