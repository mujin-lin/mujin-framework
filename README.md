# mujin-framework

> 一个基于 **Spring Boot 3.5.x + JDK 21** 的中文企业级开发框架，提供通用工具、Web 增强、缓存、安全、ORM、操作日志、接口文档等开箱即用的能力。
> 所有源代码使用 **中文注释** 与 **中文错误信息**，便于国内团队维护。

[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)
[![JDK](https://img.shields.io/badge/JDK-21%2B-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.8-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.9%2B-blue.svg)](https://maven.apache.org/)
[![Code Style](https://img.shields.io/badge/code%20style-checkstyle-success.svg)](checkstyle.xml)

---

## 📖 项目简介

`mujin-framework` 是一个面向中文团队的轻量级 Java 业务框架，采用 **Maven 多模块** 结构：

- **`mujin-commons`**：通用基础组件（无 Spring 强依赖，可被任意 Java 应用直接引用）。
- **`mujin-boot-starter`**：基于 Spring Boot 3 的自动装配（Starter）集合，业务项目引入即可获得开箱即用的能力。

## ✨ 核心特性

| 类别          | 能力                                                                          |
|---------------|-------------------------------------------------------------------------------|
| 🛠️ 通用工具    | 时间、JSON、正则、AES 加解密、异常基类、错误码                                 |
| 📄 CSV        | 对象 ↔ CSV 双向转换，支持 `@CsvProperty` / `@CsvIgnore` 自定义                 |
| 🌐 Web 增强    | CORS、请求日志、统一响应 `ResponseResult`、`@LoginCheck`、`@AccessLimit`       |
| 🔐 安全       | 校验器链（`SecurityValidator`）、请求体多次读取包装                            |
| ⚡️ 缓存       | Redis + 本地 `ExpiringMap`、`@EnableCacheCustomizer` 注解式 cacheName 与 TTL   |
| 🗃️ ORM        | MyBatis-Plus 增强：自动填充、`@SearchColumn` 动态查询、`DefaultEntity`         |
| 📜 操作日志    | `@OperationLog` AOP 织入 + 异步执行 + `@LogMask` 脱敏 + DB/FILE/Kafka 三种后端 |
| 📑 接口文档    | 基于 springdoc-openapi 2.7 + Apache PDFBox 3 的 OpenAPI 3 / Swagger UI / PDF  |

## 🔌 插件化原则

`mujin-framework` 所有 starter 遵循 **"按需启用"** 原则——**引入依赖 ≠ 启用功能**。

- 核心基础设施（`mujin-web-boot-starter`、`mujin-web-model`）：默认开启，无需配置。
- 可选功能模块（`document` / `logging` / `cache` / `orm` / `security`）：默认 **关闭**，业务方必须在 `application.yml` 显式 `enabled=true` 才生效。

| 模块 | 默认 | 启用属性 |
| --- | --- | --- |
| `mujin-web-boot-starter` | ✅ | （无开关） |
| `mujin-web-model` | ✅ | （无开关） |
| `mujin-web-document` | ❌ | `mujin.document.enabled=true` |
| `mujin-web-logging` | ❌ | `mujin.logging.enabled=true` |
| `mujin-web-cache` | ❌ | `mujin.cache.enabled=true` |
| `mujin-web-orm` | ❌ | `mujin.orm.enabled=true` |
| `mujin-web-security` | ❌ | `mujin.web.config.request.security.validator-enable=true` |

> 📘 详细的开关矩阵与扩展点参见 [`docs/architecture.md`](docs/architecture.md)。

## 📜 许可证合规

`mujin-framework` 作为被多个业务项目以**二进制依赖**形式引用的基础框架，**禁止**引入任何 **copyleft 强传染协议**（GPL / AGPL）的依赖。

- **iText 7** 自 2021 年起改用 AGPL v3，框架已切换到 **Apache PDFBox 3.0.3**（Apache License 2.0，可商用）。
- 所有依赖的许可证清单参见 [`docs/architecture.md` 附录](docs/architecture.md)。

## 📦 模块结构

```
mujin-framework
├── pom.xml                              # 父 POM：统一依赖版本与插件
│
├── mujin-commons/                       # 通用基础组件（无 Spring 强依赖）
│   ├── commons-lang/                    # 时间/JSON/正则/加密/异常/错误码
│   ├── commons-csv/                     # CSV 序列化与反序列化
│   └── commons-web/                     # Web 通用：注解/请求/响应/校验
│
├── mujin-boot-starter/                  # Spring Boot 自动装配
│   ├── mujin-web-boot-starter/          # 入口装配（CommonsProperties / CORS / 请求日志）
│   ├── mujin-web-security/              # 安全：校验器链 + 请求体包装
│   ├── mujin-web-cache/                 # 缓存：Redis / 本地 ExpiringMap
│   ├── mujin-web-orm/                   # ORM：MyBatis-Plus 增强
│   ├── mujin-web-logging/               # 操作日志：注解 / 采集器 / AOP / 异步 / FILE
│   ├── mujin-web-logging-db/            # 操作日志 DB 存储（MyBatis-Plus + 自动建表）
│   ├── mujin-web-logging-kafka/         # 操作日志 Kafka 存储
│   ├── mujin-web-document/              # 接口文档（OpenAPI + Swagger UI + PDF）
│   └── mujin-web-model/                 # 通用 DTO/VO
│
├── docs/                                # 架构与设计文档
│   └── architecture.md                  # 模块依赖 / 请求处理 / 扩展点
│
├── CHANGELOG.md                         # 版本变更记录
├── CONTRIBUTING.md                      # 贡献指南
├── checkstyle.xml                       # Checkstyle 主规则
└── rule.md                              # 项目说明 + AI 编码规范
```

## 🚀 快速开始

### 环境要求

- **JDK** 21 或以上（启用 `-parameters`）
- **Maven** 3.9+
- **Spring Boot** 3.5.x（如需运行示例工程）

### 构建项目

```bash
# 克隆仓库
git clone https://gitee.com/mujin/mujin-framework.git
cd mujin-framework

# 全量编译（含 javadoc 与 sources 打包）
mvn clean install

# 跳过 javadoc 加速本地构建
mvn clean install -Dmaven.javadoc.skip=true

# 仅构建指定模块及其依赖
mvn -pl mujin-commons/commons-lang -am clean install
```

---

## 🧩 模块配置与使用

### 1. `commons-lang` — 通用基础工具

无 Spring 依赖，可被任意 Java 项目直接引入。

#### 引入依赖

```xml
<dependency>
    <groupId>com.mujin.commons</groupId>
    <artifactId>commons-lang</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

#### 关键 API

| 工具类 | 主要方法 | 说明 |
|--------|----------|------|
| `JsonUtil` | `toJson(obj)` / `toObject(text, Class)` / `toJsonNode(text)` / `jsonMapper()` | Jackson 封装，支持 Java8 时间、自定义日期格式 |
| `DateTimeUtils` | `objectToDate(Object)` / `format(Date)` / `parse(String)` | 多源时间解析（Long / String / Date） |
| `RegexUtils` | `isPhone(String)` / `isEmail(String)` / `isIdCard(String)` / `isPositiveInteger(String)` | 常用正则校验 |
| `EncryptUtils` | `aesEncrypt(content, key)` / `aesDecrypt(content, key)` / `generateKey(algorithm)` / `toSha1(str)` / `base64Encode/Decode` | AES 对称加密、Base64、SHA1 |
| `CommonsException` / `BusinessException` / `FrameworkException` | 三种构造器 | 业务/框架异常基类 |
| `ErrorCodeDefinition` | `int errorCode()` | 错误码接口，枚举实现统一管理错误码 |
| `BaseErrorCode` / `BusinessErrorCode` / `ServiceErrorCode` / `FrameworkErrorCode` / `DataCheckErrorCode` / `AuthorizationErrorCode` | 内置错误码枚举 | 按业务域段划分 |

#### 示例：错误码与异常

```java
public enum OrderErrorCode implements ErrorCodeDefinition {
    ORDER_NOT_FOUND(2001, "订单不存在"),
    ORDER_STATUS_INVALID(2002, "订单状态非法");

    private final int errorCode;

    OrderErrorCode(int errorCode, String desc) {
        this.errorCode = errorCode;
    }

    @Override
    public int errorCode() {
        return this.errorCode;
    }
}

throw new BusinessException(OrderErrorCode.ORDER_NOT_FOUND, "订单 " + id + " 不存在");
```

---

### 2. `commons-csv` — CSV 序列化

#### 引入依赖

```xml
<dependency>
    <groupId>com.mujin.commons</groupId>
    <artifactId>commons-csv</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

#### 关键 API


`com.mujin.commons.csv.CsvOperateUtil` 静态方法：

| 方法 | 用途 |
|------|------|
| `read(File / InputStream / String, Class<T>, CsvHandlerConfig)` | 读取 CSV 到对象集合 |
| `write(Collection<T> / File, Class<T>, CsvHandlerConfig)` | 写出对象集合到 CSV |
| `writeString(Collection<T>, Class<T>, CsvHandlerConfig)` | 返回 CSV 字符串 |

#### 注解

- **`@CsvProperty(value, index, formatJson, dataInvokeMethod, dataSetInvokeMethod)`**：字段级，控制表头、顺序、JSON 格式化与自定义 getter/setter。
- **`@CsvIgnore`**：字段级，忽略该字段。
- **`@CsvGenerics`**：集合/泛型字段，指定内部元素类型。
- **`@CsvDateFormat(pattern)`**：日期字段格式化。

#### 示例

```java
@Data
public class UserExport {
    @CsvProperty(value = "用户ID", index = 1)
    private Long id;

    @CsvProperty(value = "姓名", index = 2)
    private String name;

    @CsvIgnore
    private String password;

    @CsvProperty(value = "创建时间", index = 3, dataInvokeMethod = "formatToString")
    @CsvDateFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}

// 写出
String csv = CsvOperateUtil.writeString(users, UserExport.class, null);

// 读取
List<UserExport> list = CsvOperateUtil.read(new File("users.csv"), UserExport.class, null);
```

---

### 3. `commons-web` — Web 通用注解与响应

#### 引入依赖

```xml
<dependency>
    <groupId>com.mujin.commons</groupId>
    <artifactId>commons-web</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

#### 关键 API

| 类型 | 路径 | 说明 |
|------|------|------|
| 注解 | `com.mujin.commons.web.annotations.LoginCheck` | 类/方法级，标记需要登录态 |
| 注解 | `com.mujin.commons.web.annotations.AccessLimit(value, maxVisits, forbiddenSecond)` | 方法级，访问限流（默认 3s 内 10 次，超出禁用 10s） |
| 模型 | `com.mujin.commons.web.response.ResponseResult<T>` | 统一响应包装（`resCode` / `resMsg` / `resData`） |
| 工具 | `com.mujin.commons.web.response.ResponseUtils` | `success(...)` / `fail(errCode, msg, detailMsg)` 快捷构造 |
| 接口 | `com.mujin.commons.web.manager.LoginUserManager` | 登录用户缓存管理（业务自行实现） |
| 接口 | `com.mujin.commons.web.model.LoginUserModel` | 登录用户接口（getAccount / getUserName / getId / getToken 等） |

#### 示例

```java
@RestController
@RequestMapping("/order")
public class OrderController {

    @PostMapping
    @LoginCheck                                     // 要求登录
    @AccessLimit(value = 5, maxVisits = 3)          // 5s 内最多 3 次
    public ResponseResult<OrderDTO> create(@RequestBody OrderCreateReq req) {
        OrderDTO order = orderService.create(req);
        return ResponseUtils.success(order);
    }
}
```

---

### 4. `mujin-web-boot-starter` — 框架入口装配

提供三套 `@ConfigurationProperties` 配置：

| 配置前缀 | 类 | 说明 |
|----------|----|------|
| `mujin.web.config.commons` | `CommonsProperties` | 加密/登录管理器类型（`DEFAULT` / `REDIS`） |
| `mujin.web.config.cors` | `CorsConfigProperties` | CORS 跨域配置（路径、origin、headers、methods、maxAge） |
| `mujin.web.config.request` | `RequestInfoPrintProperties` | 请求信息打印开关（uri/ip/os/source/browser/param/body） |

#### 引入依赖

```xml
<dependency>
    <groupId>com.mujin.boot</groupId>
    <artifactId>mujin-web-boot-starter</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

#### 完整配置示例

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

### 5. `mujin-web-cache` — 缓存

支持 Redis 与本地内存两种缓存管理器；通过 `@EnableCacheCustomizer` 注解启用。

#### 引入依赖

```xml
<dependency>
    <groupId>com.mujin.boot</groupId>
    <artifactId>mujin-web-cache</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

#### 配置项（Spring Boot 原生 + 框架扩展）

```yaml
spring:
  cache:
    type: REDIS                                # SIMPLE / REDIS / MIX
    cache-names: userCache, orderCache         # 预声明的缓存名
    redis:
      time-to-live: 10m                         # 默认 TTL（Duration）
      key-prefix: "demo:"                      # 缓存 key 前缀
      use-key-prefix: true
      cache-null-values: false                 # 是否缓存 null
      enable-statistics: false                 # 是否开启统计
```

#### 启用方式

```java
@SpringBootApplication
@EnableCacheCustomizer(
    basePackages = "com.example.app.cache",    // 扫描实现 CacheManagerCacheNameCaching 的类
    cacheType = CacheManagerEnum.REDIS,
    allowRuntimeCreation = true                // 未声明的 cacheName 是否允许运行时创建
)
public class Application { }
```

#### 自定义缓存名配置

```java
@Component
public class UserCacheConfig implements RedisCacheManagerPrefixCaching {
    @Override
    public String cacheName() { return "userCache"; }

    @Override
    public Duration expiry() { return Duration.ofMinutes(30); }

    @Override
    public String cachePrefix() { return "user:"; }

    @Override
    public RedisSerializer<String> keySerializer() { return RedisSerializer.string(); }

    @Override
    public RedisSerializer<?> valueSerializer() { return RedisSerializer.json(); }
}
```

#### 内置 `LoginUserManager` 实现

框架内置 `RedisLoginUserManager`（基于 `RedisCacheManager`），可直接通过 `@Autowired` 注入使用。

---

### 6. `mujin-web-orm` — MyBatis-Plus 增强

#### 引入依赖

```xml
<dependency>
    <groupId>com.mujin.boot</groupId>
    <artifactId>mujin-web-orm</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

#### 配置项

```yaml
mujin:
  web:
    config:
      orm:
        open-page-interceptor: true             # 分页插件（默认 true）
        optimistic-locker: false                # 乐观锁插件
        block-attack-inner: false               # 防全表更新/删除插件
        enable-auto-fill: true                  # 是否开启自动填充扫描
```

#### 启用方式

```java
@SpringBootApplication
@EnableAutoFill(basePackages = "com.example.app.entity")   // 扫描自定义 InsertFillColumnHandler / UpdateFillColumnHandler
public class Application { }
```

#### 关键类

| 类型 | 路径 | 说明 |
|------|------|------|
| 实体基类 | `com.mujin.orm.entity.BaseEntity<ID>` | 抽象基类，强制 getId/setId |
| 默认实体 | `com.mujin.orm.entity.DefaultEntity` | 含 createBy / updateBy / createTime / updateTime / delFlag（带自动填充 + 逻辑删除） |
| 搜索基类 | `com.mujin.orm.dto.SearchBase` | 动态查询条件父类 |
| 分页基类 | `com.mujin.orm.dto.PageDto<T>` | 内置 `Page<T>` + 扩展字段 |
| 搜索 DTO | `com.mujin.orm.dto.SearchPageDto` / `EntityPageDto` | 与 PageDto 配套 |
| 注解 | `@EnableAutoFill(basePackages)` | 启用自动填充扫描 |
| 注解 | `@SearchColumn(value, exist)` | 字段级，标记参与 queryWrapper 构造 |

#### 示例

```java
@Data
@EqualsAndHashCode(callSuper = true)
public class User extends DefaultEntity {
    @TableId
    private Long id;
    private String account;
    private String userName;
}

@Data
public class UserSearchDto extends SearchBase {
    @SearchColumn("user_name")           // 映射到列名 user_name
    private String userName;

    @Override
    public <T extends BaseEntity> T getWrapper() { return null; }
}
```

---

### 7. `mujin-web-security` — 校验器链

提供可插拔的校验器链，业务可在启动类注册自定义 `SecurityValidator`。

#### 引入依赖

```xml
<dependency>
    <groupId>com.mujin.boot</groupId>
    <artifactId>mujin-web-security</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

#### 配置项

```yaml
mujin:
  web:
    config:
      request:
        security:
          wrapper-enable: true           # 是否将 HttpServletRequest 包装为可多次读取 body
          validator-enable: true         # 是否启用校验器链
```

#### 启用方式

```java
@SpringBootApplication
@EnableSecurityValidator
public class Application {

    @Bean
    public SecurityValidatorConfigurer validatorConfigurer() {
        return registry -> registry.add(new LoginValidator(), 100)     // order=100
                                .add(new RateLimitValidator(), 200);
    }
}
```

#### 自定义校验器

```java
public class LoginValidator implements SecurityValidator {
    @Override
    public void validateBefore(PreHandleValidatorContext ctx) {
        if (!LoginContextHolder.isLoggedIn()) {
            throw new BusinessException(AuthorizationErrorCode.NOT_LOGGED_IN, "请先登录");
        }
    }

    @Override
    public void validateAfter(AfterHandlerValidatorContext ctx) {
        // 请求结束后清理资源
    }
}
```

---

### 8. `mujin-web-logging` — 操作日志

通过 `@OperationLog` 注解自动采集方法入参/出参/异常/上下文，落地到 DB / FILE / Kafka 三种可插拔后端。

> 完整设计参见 [`docs/logging-design.md`](docs/logging-design.md)；本节聚焦配置与使用。

#### 8.1 模块与依赖

| 模块 | 后端类型 | 引入场景 |
|------|----------|----------|
| `mujin-web-logging` | FILE（默认） | 仅文件落地，无需额外依赖 |
| `mujin-web-logging-db` | DB（MySQL） | 业务需落库，需引入 |
| `mujin-web-logging-kafka` | KAFKA | 业务需 Kafka 推送，需引入 |

```xml
<!-- 基础模块（注解 / 采集器 / 异步执行器 / FILE 存储） -->
<dependency>
    <groupId>com.mujin.boot</groupId>
    <artifactId>mujin-web-logging</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>

<!-- DB 存储 -->
<dependency>
    <groupId>com.mujin.boot</groupId>
    <artifactId>mujin-web-logging-db</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>

<!-- Kafka 存储 -->
<dependency>
    <groupId>com.mujin.boot</groupId>
    <artifactId>mujin-web-logging-kafka</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

#### 8.2 核心注解

| 注解 | 路径 | 说明 |
|------|------|------|
| `@OperationLog` | `com.mujin.logging.annotations.OperationLog` | 方法级，触发日志织入 |
| `@LogMask` | `com.mujin.logging.annotations.LogMask` | 字段级脱敏（KEEP_HEAD / KEEP_TAIL / MIDDLE / ALL） |
| `@LogIgnore` | `com.mujin.logging.annotations.LogIgnore` | 字段级不入参 |

`@OperationLog` 属性：

| 属性 | 说明 | 默认 |
|------|------|------|
| `value` | 操作描述（中文） | 必填 |
| `bizId` | SpEL 表达式，提取业务对象标识（如 `#req.orderNo`） | "" |
| `operator` | SpEL 表达式，提取操作人；留空读登录上下文 | "" |
| `saveParam` | 是否保存入参 | true |
| `saveResult` | 是否保存出参 | true |
| `slowThreshold` | 慢方法阈值（ms），超出会标记 | 3000 |

#### 8.3 完整配置（`mujin.logging.*`）

```yaml
mujin:
  logging:
    enabled: true                                  # 总开关
    storage-type: DB                               # DB / FILE / KAFKA
    async: true                                    # 是否异步写
    thread-pool-size: 4                            # 异步线程池核心
    queue-capacity: 1024                           # 队列上限（满则降级同步）
    slow-threshold: 3000                           # 全局慢方法阈值
    capture-header: true                           # 是否记录请求头
    file:
      base-path: ./logs/operation
      max-history: 30
      max-file-size: 50MB
    db:
      table-prefix: mujin_                         # 表名前缀
      auto-create-table: true                      # 启动自动建表（仅 MySQL）
      datasource-bean-name: ""                     # 留空=复用业务数据源；非空=独立数据源
    kafka:
      topic: mujin-operation-log
      bootstrap-servers: localhost:9092
```

#### 8.4 DB 模式启用

```java
@SpringBootApplication
@MapperScan(basePackages = "com.mujin.logging.db.mapper")   // 复用业务数据源时必填
public class Application { }
```

启动后自动建表：`mujin_operation_log` + `mujin_operation_param`。

#### 8.5 独立数据源

```java
@Configuration
public class DataSourceConfig {

    @Bean(name = "operationLogDataSource")
    @ConfigurationProperties("spring.datasource.operation-log")
    public DataSource operationLogDataSource() {
        return DataSourceBuilder.create().build();
    }
}
```

```yaml
mujin:
  logging:
    storage-type: DB
    db:
      datasource-bean-name: operationLogDataSource   # 指向独立 DataSource Bean
spring:
  datasource:
    operation-log:
      url: jdbc:mysql://log-db:3306/logs
      username: log
      password: ********
```

#### 8.6 Kafka 模式启用

```yaml
mujin:
  logging:
    storage-type: KAFKA
spring:
  kafka:
    bootstrap-servers: localhost:9092
```

#### 8.7 业务示例

```java
@RestController
@RequestMapping("/order")
public class OrderController {

    @PostMapping
    @OperationLog(value = "创建订单", bizId = "#req.orderNo", slowThreshold = 500)
    public Result<OrderDTO> create(@RequestBody OrderCreateReq req) {
        return orderService.create(req);
    }
}

@Data
public class OrderCreateReq {
    @LogMask(MaskType.KEEP_HEAD)              // 身份证保留头 3 位
    private String idCard;

    @LogMask(value = MaskType.MIDDLE, head = 3, tail = 4)   // 手机号保留 138****1234
    private String phone;

    @LogIgnore                                // 不入参
    private String password;

    private String orderNo;
}
```

#### 8.8 关键特性

- **异步执行**：默认异步写，队列满时降级同步执行（不丢日志）。
- **MDC 透传**：异步线程自动透传 traceId。
- **失败容忍**：写库 / Kafka 发送异常仅 warn，不污染业务。
- **自动建表**：DB 模式启动时自动创建两张表（幂等 `CREATE TABLE IF NOT EXISTS`）。
- **脱敏递归生效**：嵌套对象、集合元素、Map 值自动继承 `@LogMask` / `@LogIgnore`。

---

### 9. `mujin-web-document` — 接口文档

基于 [springdoc-openapi 2.7.0](https://springdoc.org/) + Apache PDFBox 3.x 的接口文档模块，提供 OpenAPI 3.0 自动生成、Swagger UI、JSON / YAML 规范导出、PDF 文档导出。

#### 引入依赖

```xml
<dependency>
    <groupId>com.mujin.boot</groupId>
    <artifactId>mujin-web-document</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

#### 启用方式（按需）

`mujin-web-document` 默认关闭，需在 `application.yml` 显式启用：

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
      enabled: true                     # 启用 PDF 导出（默认 false）
      engine: PDFBOX                    # PDF 生成引擎（当前仅 PDFBOX）
      font-path: /opt/fonts/simhei.ttf  # 中文字体 TTF/OTF（可选）
      page-size: A4                     # A4 / LETTER
      include-examples: true            # PDF 是否包含调用示例
      include-models: true              # PDF 是否包含数据模型
```

#### 关键特性

- **零配置扫描**：springdoc 2.7 默认扫描整个 Spring 应用上下文中所有 `@RestController` / `@RequestMapping`，第三方包（如 `com.jjj.xxx`）的 Controller **自动被识别**，无需配置 `packagesToScan`。
- **分组支持**：可选地在 `application.yml` 配置 `mujin.document.groups[]` 进行多模块分组。
- **REST 接口**：模块自动注册以下控制器（无需业务方编写）：

| 接口 | 方法 | 说明 |
| --- | --- | --- |
| `/v3/api-docs` | GET | springdoc 默认合并 OpenAPI 规范 |
| `/v3/api-docs/{group}` | GET | 分组级 OpenAPI 规范 |
| `/swagger-ui.html` | GET | springdoc 默认 Swagger UI |
| `/api-docs/groups` | GET | 获取可用分组列表 |
| `/api-docs/spec/{group}` | GET | 获取指定分组的 OpenAPI JSON |
| `/api-docs/export/json` | GET | 下载 OpenAPI JSON 文件 |
| `/api-docs/export/yaml` | GET | 下载 OpenAPI YAML 文件 |
| `/api-docs/export/pdf` | POST | 生成 PDF 文档（请求体 `ExportRequest`） |

#### 调用示例（PDF 导出）

```bash
curl -X POST http://localhost:8080/api-docs/export/pdf \
  -H "Content-Type: application/json" \
  -d '{
    "format": "PDF",
    "includeExamples": true,
    "includeModels": true,
    "pageSize": "A4",
    "languages": ["curl", "java", "python", "javascript"]
  }' \
  -o api-document.pdf
```

#### 中文字体配置

PDF 默认使用 PDFBox 内置 Helvetica，**不支持中文**。需提供 TTF 字体文件：

```yaml
mujin:
  document:
    pdf-export:
      font-path: classpath:fonts/simhei.ttf
```

或者绝对路径 `font-path: /usr/share/fonts/simhei.ttf`。字体未找到时自动降级到 Helvetica（中文显示 `?`）。

#### 与 springdoc 的关系

`mujin-web-document` 不重新扫描 Controller，而是**复用 springdoc 生成的 `OpenAPI` Bean**。这意味着：

- 业务方无需引入额外的 `springdoc-openapi-starter-webmvc-ui` 依赖（`mujin-web-document` 已经传递引入）。
- 所有 springdoc 注解（`@Operation` / `@Parameter` / `@Schema` / `@Tag` 等）天然生效。

---

### 10. `mujin-web-model` — 通用 DTO/VO

当前为占位模块，预留承载跨 starter 共享的响应包装 `ResponseResult<T>`、分页模型等基础类型。

> 当前响应包装位于 `commons-web` 的 `com.mujin.commons.web.response.ResponseResult`，未来可能迁移至本模块。

---

## 📋 完整 application.yml 模板

下面整合所有 starter 的核心配置，可直接拷贝使用。**注意：可选模块默认关闭，需显式启用**：

```yaml
spring:
  application:
    name: demo-app

  # ===== 缓存（mujin-web-cache，需启用）=====
  cache:
    type: REDIS
    cache-names: userCache, orderCache
    redis:
      time-to-live: 10m
      key-prefix: "demo:"
      use-key-prefix: true
      cache-null-values: false
      enable-statistics: false

  # ===== 数据库（业务自身）=====
  datasource:
    url: jdbc:mysql://localhost:3306/demo
    username: root
    password: ********
    driver-class-name: com.mysql.cj.jdbc.Driver

  # ===== Kafka（仅 Kafka 模式需要）=====
  kafka:
    bootstrap-servers: localhost:9092

mujin:
  # ===== 入口（mujin-web-boot-starter 默认启用）=====
  web:
    config:
      commons:
        encrypt-manager-type: DEFAULT
        login-user-manager-type: DEFAULT
      cors:
        enable-cors: true
        mapping-path-pattern: /**
        allowed-origin-patterns: ["*"]
        allowed-methods: [GET, POST, PUT, DELETE, OPTIONS]
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
      # 安全（mujin-web-security，需 mujin.security.enabled=true）
      request.security:
        wrapper-enable: true
        validator-enable: true
      # ORM（mujin-web-orm，需 mujin.orm.enabled=true）
      orm:
        open-page-interceptor: true
        optimistic-locker: false
        block-attack-inner: false
        enable-auto-fill: true

  # ===== 操作日志（mujin-web-logging，需显式启用）=====
  logging:
    enabled: true                        # ⚠️ 默认 false
    storage-type: DB                     # DB / FILE / KAFKA
    async: true
    thread-pool-size: 4
    queue-capacity: 1024
    slow-threshold: 3000
    capture-header: true
    file:
      base-path: ./logs/operation
      max-history: 30
      max-file-size: 50MB
    db:
      table-prefix: mujin_
      auto-create-table: true
      datasource-bean-name: ""           # 留空=复用业务数据源
    kafka:
      topic: mujin-operation-log
      bootstrap-servers: localhost:9092

  # ===== 缓存（mujin-web-cache，需显式启用）=====
  cache:
    enabled: true                        # ⚠️ 默认 false

  # ===== ORM 增强（mujin-web-orm，需显式启用）=====
  orm:
    enabled: true                        # ⚠️ 默认 false

  # ===== 接口文档（mujin-web-document，需显式启用）=====
  document:
    enabled: true                        # ⚠️ 默认 false
    title: "业务系统 API 文档"
    version: "1.0.0"
    swagger-ui:
      path: /doc.html
      enabled: true
    pdf-export:
      enabled: true                      # ⚠️ 默认 false
      engine: PDFBOX
      font-path: /opt/fonts/simhei.ttf   # 中文字体（可选）
      page-size: A4
```

启动类：

```java
@SpringBootApplication
@EnableCacheCustomizer(basePackages = "com.example.app.cache")
@EnableAutoFill(basePackages = "com.example.app.entity")
@EnableSecurityValidator
@MapperScan(basePackages = {
    "com.example.app.mapper",                          // 业务 Mapper
    "com.mujin.logging.db.mapper"                      // 操作日志 Mapper（DB 模式需要）
})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

---

## ❓ 常见问题（FAQ）

**Q1：模块那么多，必须全部引入吗？**  
按需引入：`commons-lang` 是基础，几乎所有业务都需要；其他 starter 按业务场景选择。**引入依赖 ≠ 启用功能**，可选模块默认关闭，需 `application.yml` 显式 `enabled=true`。

**Q2：操作日志如何关闭某个方法？**  
不写 `@OperationLog` 注解即可；也可以设置全局 `mujin.logging.enabled=false` 关闭整个模块。

**Q3：脱敏 `@LogMask` 对嵌套字段生效吗？**  
生效。`ParamJsonSerializer` 基于 Jackson `BeanSerializerModifier`，对每个 Bean 的字段递归检查注解，嵌套对象 / 集合元素 / Map 值均自动继承。

**Q4：操作日志如何与现有 traceId 体系对接？**  
`WebContextCollector` 优先从请求头 `X-Trace-Id` 读取，业务侧在过滤器中写入即可；异步线程通过 `MdcTaskDecorator` 自动透传 MDC。

**Q5：DB 模式下表已经存在，自动建表会破坏数据吗？**  
不会。`OperationLogSchemaInitializer` 使用 `CREATE TABLE IF NOT EXISTS`，幂等且不影响已有数据。

**Q6：自定义校验器如何控制顺序？**  
注册时指定 order：`registry.add(myValidator, 100)`，order 越小越先执行。

**Q7：mujin-web-document 引入后，为什么 `/v3/api-docs` 是空的？**  
需在 `application.yml` 设置 `mujin.document.enabled=true`。`mujin-web-document` 默认关闭——这是插件化原则，不开启就不会注入 `OpenAPI` Bean。

**Q8：如何替换框架默认的 collector / LogStorage？**  
实现对应接口后声明为 Spring Bean，框架通过 `@ConditionalOnMissingBean` 自动让位。

**Q9：第三方项目（`com.jjj.xxx`）的 Controller 会被 mujin-web-document 自动扫描到吗？**  
会。`mujin-web-document` 完全依赖 springdoc 2.7 的默认行为——扫描整个 Spring 应用上下文中所有 `@RestController` / `@RequestMapping`，无需配置 `packagesToScan`。springdoc 默认在 `/v3/api-docs` 暴露合并后的 OpenAPI，PDF 导出也会包含第三方接口。

**Q10：PDF 导出时中文显示为 `?`，如何修复？**  
PDFBox 内置 Helvetica 不支持中文。需在 `mujin.document.pdf-export.font-path` 配置 TTF 字体路径（如 `/usr/share/fonts/simhei.ttf` 或 `classpath:fonts/simhei.ttf`）。字体未找到时自动降级，中文会显示 `?`。

**Q11：为什么用 Apache PDFBox 而非 iText 7？**  
iText 7 自 2021 年起改用 AGPL v3 许可证，强传染协议——任何"通过网络向用户提供"的应用（即便内网）必须开源。`mujin-framework` 作为被多个业务项目以二进制形式引用的基础框架，禁止引入此类依赖。PDFBox 3.x 是 Apache License 2.0，可商用、无传染性。详见 [`docs/architecture.md` §许可证合规](docs/architecture.md)。

---

## 📢 约定速览

> 新人必读：所有自定义注解统一放在 `annotations` 包，保持一致。详细规则见 [`rule.md`](rule.md)。

- **包名前缀**：公共模块 `com.mujin.commons.*`；starter 模块 `com.mujin.<业务子包>`（如 `com.mujin.logging`）。
- **类命名**：`UpperCamelCase`；接口不加 `I` 前缀；实现类可加后缀（`Handler`、`Manager`、`Storage`）。
- **Lombok**：业务模型优先 `@Data`；工具类**不要**用 Lombok，构造方法必须私有化。
- **异常**：必须复用 `commons-lang` 的 `BusinessException` / `FrameworkException` / `CommonsException`，按业务域段划分错误码。
- **Spring 装配**：`XxxAutoConfiguration` 配套 `@Configuration` + 必要的 `@ConditionalOnXxx`，注册到 `META-INF/spring/.../AutoConfiguration.imports`。
- **插件化**：可选 starter 必须使用 `@ConditionalOnProperty(..., matchIfMissing = false)`，业务方显式启用。
- **许可证合规**：禁止引入 AGPL / GPL 强传染协议依赖（如 iText 7）。

---

## 🧱 技术栈

| 维度 | 版本 | 备注 |
| --- | --- | --- |
| JDK | 21 | 启用 `-parameters` |
| Spring Boot | 3.5.8 | 父 BOM 统一管理 |
| Lombok | 1.18.42 | 必须使用，注解处理器已配置 |
| Hutool | 5.8.41 | 工具库，引入 hutool-bom |
| Jackson | 2.19.2 | databind + jsr310 |
| commons-lang3 | 3.20.0 | Apache Commons Lang |
| MyBatis-Plus | 3.5.15 | ORM（仅 mujin-web-orm 引入） |
| ExpiringMap | 0.5.11 | 本地过期缓存 |
| springdoc-openapi | 2.7.0 | OpenAPI 3 / Swagger UI |
| Apache PDFBox | 3.0.3 | PDF 导出（仅 mujin-web-document） |
| Apache POI / OpenHTMLtoPDF | — | 后续版本可选 |

## 📏 代码风格与规范

| 文件 | 说明 |
| --- | --- |
| [`rule.md`](rule.md) | 项目说明 + 8 段代码示例（AI 必读） |
| [`checkstyle.xml`](checkstyle.xml) | Checkstyle 主规则 |
| [`suppressions.xml`](suppressions.xml) | Checkstyle 抑制规则 |
| [`.editorconfig`](.editorconfig) | 跨 IDE 缩进/编码/行尾 |
| [`.idea/codeStyles/Project.xml`](.idea/codeStyles/Project.xml) | IDEA Code Style |
| [`.idea/fileTemplates/internal/*`](.idea/fileTemplates/internal) | IDEA 文件模板 |
| [`.idea/templates/mujin-framework.xml`](.idea/templates/mujin-framework.xml) | IDEA Live Template |
| [`CONTRIBUTING.md`](CONTRIBUTING.md) | 贡献指南（分支 / 提交 / PR） |
| [`SETUP.md`](SETUP.md) | 在 IDEA / VSCode / Eclipse 中启用配置 |
| [`docs/logging-design.md`](docs/logging-design.md) | 操作日志详细设计 |
| [`docs/architecture.md`](docs/architecture.md) | 架构图与扩展点 |
| [`CHANGELOG.md`](CHANGELOG.md) | 版本变更记录 |

CI 默认执行 `mvn checkstyle:check`，未通过禁止合并。

---

## 🤝 贡献指南

欢迎通过 Issue 反馈问题、提交 PR 贡献代码：

1. Fork 仓库，从 `develop` 拉取 `feature/<name>` 分支；
2. 遵循 [rule.md](rule.md)、[checkstyle.xml](checkstyle.xml) 与 [CONTRIBUTING.md](CONTRIBUTING.md)；
3. 提交前 `mvn clean verify -Dmaven.javadoc.skip=true` 自检；
4. PR 通过 1 位 reviewer 评审 + CI 全绿后合入。

详细的提交规范、PR 流程、版本号策略请查阅 [CONTRIBUTING.md](CONTRIBUTING.md)。

## 📝 版本与发布

- 当前版本：`1.0.0-SNAPSHOT`
- 版本节奏：迭代开发合入 `develop`，发布时从 `develop` 拉取 `release/<version>` 分支。
- 历史版本参见 [Releases](https://gitee.com/mujin/mujin-framework/releases)。

## 📄 License

本项目基于 [Apache License 2.0](LICENSE) 开源。

> 所有依赖均为 Apache 2.0 / MIT / LGPL 等宽松许可证，**不含 AGPL / GPL 强传染协议**。
> 历史版本曾使用的 iText 7（AGPL v3）已替换为 Apache PDFBox 3.x。

## 👥 维护者

- 主要开发者：`chenglin.wu`
- 反馈邮箱：mujinlin_lin@163.com

---

如果本项目对你有帮助，欢迎 ⭐ Star，你的支持是我们持续迭代的最大动力。
