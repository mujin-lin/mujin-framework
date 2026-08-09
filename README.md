# mujin-framework

> 一个基于 **Spring Boot 3.5.x + JDK 21** 的中文企业级开发框架，提供通用工具、Web 增强、缓存、安全、ORM、操作日志等开箱即用的能力。
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
└── mujin-boot-starter/                  # Spring Boot 自动装配
    ├── mujin-web-boot-starter/          # 入口装配（CommonsProperties / CORS / 请求日志）
    ├── mujin-web-security/              # 安全：校验器链 + 请求体包装
    ├── mujin-web-cache/                 # 缓存：Redis / 本地 ExpiringMap
    ├── mujin-web-orm/                   # ORM：MyBatis-Plus 增强
    ├── mujin-web-logging/               # 操作日志：注解 / 采集器 / AOP / 异步 / FILE
    ├── mujin-web-logging-db/            # 操作日志 DB 存储（MyBatis-Plus + 自动建表）
    ├── mujin-web-logging-kafka/         # 操作日志 Kafka 存储
    ├── mujin-web-document/              # 接口文档（规划中）
    └── mujin-web-model/                 # 通用 DTO/VO（占位，规划中）
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

> 本机 Maven 路径参考 `~/.claude/projects/.../memory/maven-path.md`。

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

### 9. `mujin-web-document` / `mujin-web-model`（规划中）

两个模块当前为占位状态，尚未提供代码。后续将分别承载接口文档生成与通用 DTO/VO 模型。

---

## 📋 完整 application.yml 模板

下面整合所有 starter 的核心配置，可直接拷贝使用：

```yaml
spring:
  application:
    name: demo-app

  # ===== 缓存（mujin-web-cache）=====
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
  # ===== 入口（mujin-web-boot-starter）=====
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
      # 安全（mujin-web-security）
      request.security:
        wrapper-enable: true
        validator-enable: true
      # ORM（mujin-web-orm）
      orm:
        open-page-interceptor: true
        optimistic-locker: false
        block-attack-inner: false
        enable-auto-fill: true

  # ===== 操作日志（mujin-web-logging）=====
  logging:
    enabled: true
    storage-type: DB                       # DB / FILE / KAFKA
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
      datasource-bean-name: ""             # 留空=复用业务数据源
    kafka:
      topic: mujin-operation-log
      bootstrap-servers: localhost:9092
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
按需引入：`commons-lang` 是基础，几乎所有业务都需要；其他 starter 按业务场景选择。

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

**Q8：如何替换框架默认的 collector / LogStorage？**  
实现对应接口后声明为 Spring Bean，框架通过 `@ConditionalOnMissingBean` 自动让位。

---

## 📢 约定速览

> 新人必读：所有自定义注解统一放在 `annotations` 包，保持一致。详细规则见 [`rule.md`](rule.md)。

- **包名前缀**：公共模块 `com.mujin.commons.*`；starter 模块 `com.mujin.<业务子包>`（如 `com.mujin.logging`）。
- **类命名**：`UpperCamelCase`；接口不加 `I` 前缀；实现类可加后缀（`Handler`、`Manager`、`Storage`）。
- **Lombok**：业务模型优先 `@Data`；工具类**不要**用 Lombok，构造方法必须私有化。
- **异常**：必须复用 `commons-lang` 的 `BusinessException` / `FrameworkException` / `CommonsException`，按业务域段划分错误码。
- **Spring 装配**：`XxxAutoConfiguration` 配套 `@Configuration` + 必要的 `@ConditionalOnXxx`，注册到 `META-INF/spring/.../AutoConfiguration.imports`。

---

## 🧱 技术栈

| 维度         | 版本                |
|--------------|---------------------|
| JDK          | 21                  |
| Spring Boot  | 3.5.8               |
| Lombok       | 1.18.42             |
| Hutool       | 5.8.41              |
| Jackson      | 2.19.2              |
| commons-lang3| 3.20.0              |
| MyBatis-Plus | 3.5.15              |
| ExpiringMap  | 0.5.11              |

## 📏 代码风格与规范

| 文件                                       | 说明                                   |
|--------------------------------------------|----------------------------------------|
| [`rule.md`](rule.md)                   | 项目说明 + 8 段代码示例（AI 必读）     |
| [`checkstyle.xml`](checkstyle.xml)         | Checkstyle 主规则                      |
| [`suppressions.xml`](suppressions.xml)     | Checkstyle 抑制规则                    |
| [`.editorconfig`](.editorconfig)           | 跨 IDE 缩进/编码/行尾                  |
| [`.idea/codeStyles/Project.xml`](.idea/codeStyles/Project.xml) | IDEA Code Style            |
| [`.idea/fileTemplates/internal/*`](.idea/fileTemplates/internal) | IDEA 文件模板             |
| [`.idea/templates/mujin-framework.xml`](.idea/templates/mujin-framework.xml) | IDEA Live Template |
| [`CONTRIBUTING.md`](CONTRIBUTING.md)       | 贡献指南（分支 / 提交 / PR）           |
| [`SETUP.md`](SETUP.md)                     | 在 IDEA / VSCode / Eclipse 中启用配置  |
| [`docs/logging-design.md`](docs/logging-design.md) | 操作日志详细设计             |

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

## 👥 维护者

- 主要开发者：`chenglin.wu`
- 反馈邮箱：mujinlin_lin@163.com

---

如果本项目对你有帮助，欢迎 ⭐ Star，你的支持是我们持续迭代的最大动力。
