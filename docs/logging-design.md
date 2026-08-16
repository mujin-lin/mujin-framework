# mujin-web-logging 设计文档（待审阅）

> 本文档为 `mujin-web-logging` 模块的设计方案，**未落地任何代码**。请逐项勾选，确认后再开始实施。

---

## 1. 目标与定位

提供一个 `@OperationLog` 注解，开启后自动采集**被标注方法**的入参、出参、耗时、异常与上下文，落地到**数据库 / 日志文件 / Kafka** 三种可插拔后端中的一种。

### 1.1 核心特性

| 能力                       | 说明                                                                 |
|----------------------------|----------------------------------------------------------------------|
| 注解驱动                   | `@OperationLog("保存订单")` 一行开启                                 |
| 入参 / 出参采集            | JSON 序列化，可按 `@LogMask` 字段脱敏                                |
| SpEL 表达式                | 通过 `#userId`、`#req.id` 提取关键字段作为「操作对象」               |
| 异步非阻塞                 | `@Async` + 自管理线程池，写库不阻塞业务                              |
| Web 上下文                 | 自动写入 IP / URI / User-Agent / 当前登录用户                        |
| Trace 串联                 | 与 MDC traceId 联动，写入 `trace_id` 字段                            |
| 多后端可插拔               | 数据库（MySQL）/ 日志文件（Logback）/ Kafka（topic）                 |
| 独立数据源                 | 可选 `mujin.logging.datasource.*` 与业务库隔离                       |
| 失败容忍                   | 日志写库失败不能拖垮业务，仅记录告警                                 |

### 1.2 不在 MVP 范围

- 跨服务链路追踪（依赖 OpenTelemetry / SkyWalking 单独集成）；
- 审计工作流审批流；
- 操作日志回滚（仅追加）。

---

## 2. 模块坐标

```xml
<groupId>com.mujin.boot</groupId>
<artifactId>mujin-web-logging</artifactId>
<version>1.0.0-SNAPSHOT</version>
<packaging>jar</packaging>
```

> 同时**启用**父 POM 的 `<modules>` 与 `<dependencyManagement>`（此前为占位模块）。

---

## 3. 包结构

```
com.mujin.logging
├── annotations/
│   ├── OperationLog.java          // 方法级注解
│   ├── LogMask.java               // 字段级注解（敏感数据脱敏）
│   └── LogIgnore.java             // 字段级注解（不入参）
├── enums/
│   ├── LogStorageType.java        // DB / FILE / KAFKA
│   └── LogResultEnum.java         // SUCCESS / FAIL
├── configuration/
│   ├── LoggingProperties.java     // prefix=mujin.logging
│   └── LoggingDatasourceProperties.java
├── model/
│   ├── OperationLogEntity.java    // 实体：operation_log
│   ├── OperationLogParamEntity.java // 实体：operation_log_param
│   └── OperationLogContext.java   // 运行期上下文
├── aop/
│   └── OperationLogAspect.java    // 织入 @OperationLog 方法
├── collector/
│   ├── RequestContextCollector.java
│   ├── TraceContextCollector.java
│   ├── AsyncContextCollector.java
│   └── ParamCollector.java        // 含 @LogMask / @LogIgnore 处理
├── serializer/
│   └── ParamJsonSerializer.java   // 复用 commons-lang 的 JsonUtil + 脱敏
├── persistence/
│   ├── LogStorage.java            // 策略接口
│   ├── DbLogStorage.java          // JDBC / MyBatis-Plus 实现
│   ├── FileLogStorage.java        // Logback JSON 行输出
│   └── KafkaLogStorage.java       // KafkaTemplate 实现
├── executor/
│   └── LoggingExecutor.java       // 异步线程池
└── auto/
    └── LoggingAutoConfiguration.java
```

---

## 4. 注解设计

### 4.1 `@OperationLog`（方法级）

```java
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {
    /** 操作描述（中文） */
    String value();

    /** SpEL：操作对象标识，如 #req.orderId */
    String bizId() default "";

    /** SpEL：操作人标识，默认从登录上下文取 */
    String operator() default "";

    /** 是否保存入参，默认 true */
    boolean saveParam() default true;

    /** 是否保存出参，默认 true */
    boolean saveResult() default true;

    /** 慢方法阈值（ms），超过则单独标记 */
    long slowThreshold() default 3000L;
}
```

### 4.2 `@LogMask`（字段级）

```java
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface LogMask {
    /** 脱敏策略：KEEP_HEAD / KEEP_TAIL / MIDDLE / ALL */
    MaskType value() default MaskType.KEEP_HEAD;
    int head() default 3;
    int tail() default 4;
}
```

### 4.3 `@LogIgnore`（字段级）

```java
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface LogIgnore { }
```

---

## 5. 配置项（`mujin.logging.*`）

```yaml
mujin:
  logging:
    enabled: true                          # 总开关
    storage-type: DB                       # DB / FILE / KAFKA
    async: true                            # 是否异步写
    thread-pool-size: 4                    # 异步线程池大小
    queue-capacity: 1024                   # 队列上限（满则降级同步）
    slow-threshold: 3000                   # 全局慢方法阈值
    include-packages: com.example.app      # AOP 扫描包，未配置默认全部
    request:
      include-headers: [User-Agent, X-Forwarded-For]
      capture-body: true                   # body 内容入参是否落库
    file:
      base-path: ./logs/operation
      max-history: 30                      # 历史保留天数
    db:
      table-prefix: mujin_                 # 表名前缀
      auto-create-table: true              # 启动自动建表（仅 MySQL）
      datasource-bean-name: ""             # 留空=复用业务数据源
    kafka:
      topic: mujin-operation-log
      bootstrap-servers: localhost:9092
```

---

## 6. 数据库设计

> 仅当 `storage-type=DB` 且 `auto-create-table=true` 时启动初始化。

### 6.1 `operation_log`

| 字段                | 类型            | 说明                         |
|---------------------|-----------------|------------------------------|
| `id`                | BIGINT PK AUTO  |                              |
| `trace_id`          | VARCHAR(64)     | 链路追踪 ID                   |
| `biz_id`            | VARCHAR(128)    | SpEL 解析后的业务对象         |
| `module`            | VARCHAR(64)     | 类名                         |
| `method`            | VARCHAR(128)    | 方法签名                     |
| `description`       | VARCHAR(255)    | 操作描述                     |
| `operator`          | VARCHAR(64)     | 操作人                       |
| `request_uri`       | VARCHAR(255)    |                              |
| `http_method`       | VARCHAR(8)      |                              |
| `client_ip`         | VARCHAR(64)     |                              |
| `user_agent`        | VARCHAR(255)    |                              |
| `request_headers`   | TEXT            | JSON                         |
| `result`            | TINYINT         | 1=成功 0=失败                |
| `error_message`     | TEXT            | 异常堆栈摘要                 |
| `cost_ms`           | BIGINT          | 耗时                         |
| `is_slow`           | TINYINT         | 是否慢方法                   |
| `create_time`       | DATETIME        |                              |

### 6.2 `operation_log_param`

| 字段              | 类型            | 说明                |
|-------------------|-----------------|---------------------|
| `id`              | BIGINT PK AUTO  |                      |
| `log_id`          | BIGINT          | 关联 operation_log  |
| `param_type`      | VARCHAR(16)     | IN / OUT            |
| `param_index`     | INT             | 参数顺序            |
| `param_name`      | VARCHAR(128)    |                      |
| `param_value`     | LONGTEXT        | JSON / 字符串        |

> 两表通过 `log_id` 关联；参数以 JSON 形式存 `LONGTEXT`，便于复杂对象。

---

## 7. 织入流程（Aspect）

```
@OperationLog 方法被调用
       │
       ▼
[Before] 解析注解 → 构建 OperationLogContext
       │   ├─ SpEL 解析 bizId/operator
       │   ├─ 收集 Web 上下文（IP/URI/Headers）
       │   ├─ 收集 traceId（MDC）
       │   └─ 序列化入参（脱敏）
       ▼
[Proceed] 执行原方法 → 拿到 result / 异常
       │
       ▼
[After] 序列化出参（脱敏）
       │
       ▼
[Finally] 提交到 LoggingExecutor（异步）→ 落库 / 落盘 / Kafka
       └─ 失败仅打 warn 日志，不抛业务异常
```

关键设计：

- 用 `try-finally` 确保无论成功失败都记录；
- 用 `LoggingExecutor` 单线程池串行写，避免并发写库阻塞业务；
- 同步降级：队列满时退化为同步写，保证不丢；
- `Throwable` 统一捕获，转换为 `LogResultEnum.FAIL`。

---

## 8. 独立数据源切换

- 当 `mujin.logging.db.datasource-bean-name` 不为空时，从 Spring 容器取指定 Bean 作为日志数据源；
- 为空时，复用业务 `DataSource`；
- 自动装配逻辑：

```java
@Bean
@ConditionalOnProperty(prefix = "mujin.logging.db", name = "datasource-bean-name", matchIfMissing = true)
public DataSource loggingDataSource(@Value("${mujin.logging.db.datasource-bean-name:}") String name) {
    return StringUtils.isBlank(name)
            ? applicationContext.getBean(DataSource.class)
            : applicationContext.getBean(name, DataSource.class);
}
```

- 仅引入 MyBatis-Plus 依赖；不引入 Spring Boot JPA；
- 自动建表使用 `mybatis-plus.extension.schema.SchemaManager` 或 `ddl-auto=update` 都不太合适（MyBatis-Plus 不带自动建表），改用 `mysql-connector-j` 自带的 `DatabaseMetaData` + `CREATE TABLE IF NOT EXISTS` SQL。

---

## 9. 异步 / Trace 串联

### 9.1 异步

- `@EnableAsync` + 自定义 `LoggingTaskExecutor`；
- 线程池：`core=4, max=8, queue=1024, policy=CallerRuns`；
- 异常处理：`RejectedExecutionHandler` 触发同步降级。

### 9.2 Trace

- 与 `org.slf4j.MDC` 联动；
- 业务侧可自行注入 `traceId`（如 `CommonsWebFilter` 中写入），本模块**只读不写**；
- 异步线程池使用 `TaskDecorator` 把 MDC 透传到子线程。

### 9.3 Web 上下文

- 通过 `RequestContextHolder.getRequestAttributes()` 获取；
- 仅在线程池主线程读一次，缓存到 `OperationLogContext`，异步写库时复用；
- 无 Web 请求上下文（如 Service 内部调用）则留空，不强制。

---

## 10. 存储策略接口

```java
public interface LogStorage {
    /** 持久化一条日志 */
    void save(OperationLogContext context);
}
```

- `DbLogStorage`：通过 MyBatis-Plus `BaseMapper<OperationLogEntity>.insert(...)` 写入；
- `FileLogStorage`：使用 Logback `RollingFileAppender`，输出 JSON 行；
- `KafkaLogStorage`：使用 `KafkaTemplate.send(topic, key, json)`；
- 通过 `@ConditionalOnProperty(prefix = "mujin.logging", name = "storage-type", havingValue = "DB")` 等条件装配。

---

## 11. 依赖（pom.xml）

| 依赖                          | 说明                | 是否可选     |
|-------------------------------|---------------------|--------------|
| `spring-boot-starter-aop`     | AOP 织入            | 必选         |
| `spring-boot-starter-jdbc`    | JdbcTemplate        | 必选         |
| `mybatis-plus-spring-boot3-starter` | DB 写入       | 仅 DB 模式   |
| `mysql-connector-j`           | 建表                | 仅 DB 模式   |
| `com.mujin.boot:mujin-web-boot-starter` | Web 上下文 | 必选         |
| `com.mujin.commons:commons-lang`         | JsonUtil  | 必选         |
| `org.projectlombok:lombok`    | 注解                | optional     |
| `spring-kafka`                | Kafka               | 仅 Kafka 模式 |
| `logback-classic`             | JSON 日志           | Spring 已带  |

---

## 12. 自动装配

`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`：

```
com.mujin.logging.auto.LoggingAutoConfiguration
```

`LoggingAutoConfiguration` 关键内容：

- `@Configuration`
- `@EnableConfigurationProperties({LoggingProperties.class, LoggingDatasourceProperties.class})`
- `@ConditionalOnProperty(prefix = "mujin.logging", name = "enabled", matchIfMissing = true)`
- `@EnableAsync`（异步支持）
- 注入：`Aspect`、`LogStorage`（条件装配）、`Executor`、可选 `DataSource`

---

## 13. 使用示例

```java
@RestController
@RequestMapping("/order")
public class OrderController {

    @PostMapping
    @OperationLog(value = "创建订单", bizId = "#req.orderNo", saveResult = true)
    public Result<OrderDTO> create(@RequestBody OrderCreateReq req) {
        return orderService.create(req);
    }
}

@Data
public class OrderCreateReq {
    private String orderNo;

    @LogMask(MaskType.KEEP_HEAD)
    private String idCard;

    @LogIgnore
    private String password;
}
```

效果：调用 `/order` 后，自动在 `operation_log` 表里新增一条记录，`operation_log_param` 写入入参（idCard 头 3 + `****` 尾 4），并跳过 password。

---

## 14. 实施步骤（分阶段）

| 阶段 | 内容                                                                  | 工作量 |
|------|-----------------------------------------------------------------------|--------|
| L1   | 模块骨架 + pom + 注解 + LoggingProperties + AOP 空实现                | 0.5d   |
| L2   | ParamCollector（脱敏 + 忽略）+ RequestContextCollector + TraceContext | 0.5d   |
| L3   | FileLogStorage + DbLogStorage + 异步执行器                            | 1d     |
| L4   | KafkaLogStorage + 独立数据源切换                                      | 0.5d   |
| L5   | 自动建表（CREATE TABLE IF NOT EXISTS）                                | 0.5d   |
| L6   | LoggingAutoConfiguration + `AutoConfiguration.imports`                 | 0.5d   |
| L7   | 写 3 个单测（脱敏 / SpEL / 异步写库）+ 文档                           | 0.5d   |

总计 **~4d**。

---

## 15. 风险与依赖

| 风险                                  | 缓解                                                                  |
|---------------------------------------|-----------------------------------------------------------------------|
| MyBatis-Plus 不带自动建表             | 手写 `CREATE TABLE IF NOT EXISTS` + `DatabaseMetaData` 校验            |
| 异步线程池积压                        | 队列满时降级同步；监控队列大小                                        |
| 写库失败拖垮业务                      | try-catch 全捕获，仅打印 warn 日志                                    |
| `@LogMask` 对集合/嵌套字段不生效      | 递归脱敏，使用反射 + JSON 节点访问                                    |
| SpEL 解析失败                         | 降级为原表达式字符串                                                  |
| 与现有 `mujin-web-cache` / `mujin-web-orm` 的依赖冲突 | 通过 `<optional>` 控制                                                |

---

## 17. 实施记录

> 本节记录自 L1-L7 实施过程中的实际决策与产出，**以代码为准，文档可能略有滞后**。

### 17.1 实施状态

| 阶段 | 内容 | 状态 |
|------|------|------|
| L1 | 模块骨架 + pom + 注解 + LoggingProperties + AOP 空实现 | ✅ 完成 |
| L2 | ParamCollector（脱敏 + 忽略）+ 上下文采集器链 | ✅ 完成 |
| L3 | LoggingExecutor + FileLogStorage + DbLogStorage | ✅ 完成 |
| L4 | KafkaLogStorage + 独立数据源切换 | ✅ 完成 |
| L5 | 自动建表（CREATE TABLE IF NOT EXISTS） | ✅ 完成 |
| L6 | LoggingAutoConfiguration + imports 注册 | ✅ 完成 |
| L7 | 单元测试与文档 | ✅ 完成 |

### 17.2 模块变更

原方案为单模块 `mujin-web-logging`，实际落地拆分为三个 Maven 子模块：

```
mujin-boot-starter/
├── mujin-web-logging/             注解、采集器、Aspect、LoggingExecutor、FileLogStorage
├── mujin-web-logging-db/          OperationLogEntity / Mapper / DbLogStorage / Schema 初始化
└── mujin-web-logging-kafka/       KafkaLogStorage
```

拆分原因：
- MyBatis-Plus 与 MySQL 驱动从 `optional` 升级为强依赖，单独剥离可避免 FILE/Kafka-only 业务被强拽。
- spring-kafka 仅在 Kafka 子模块作为强依赖，主模块不污染 Kafka 类路径。
- Mapper 由业务方通过 `@MapperScan(basePackages = "com.mujin.logging.db.mapper")` 显式扫描；独立数据源场景下由 `LoggingDbAutoConfiguration` 自动创建 MapperFactoryBean。

### 17.3 业务接入示例

#### 17.3.1 DB 模式（默认 + 复用业务数据源）

```xml
<dependency>
    <groupId>com.mujin.boot</groupId>
    <artifactId>mujin-web-logging-db</artifactId>
</dependency>
```
```java
@SpringBootApplication
@MapperScan(basePackages = "com.mujin.logging.db.mapper")
public class MyApp { }
```
```yaml
mujin:
  logging:
    storage-type: DB
    async: true
```

#### 17.3.2 DB 模式（独立数据源）

```yaml
mujin:
  logging:
    storage-type: DB
    db:
      datasource-bean-name: operationLogDataSource
```
业务提供 `operationLogDataSource` 的 `DataSource` Bean 即可，Mapper / SqlSessionFactory / TransactionManager 由本模块自动装配。

#### 17.3.3 FILE 模式

```yaml
mujin:
  logging:
    storage-type: FILE
    file:
      base-path: ./logs/operation
      max-history: 30
      max-file-size: 50MB
```

#### 17.3.4 Kafka 模式

```xml
<dependency>
    <groupId>com.mujin.boot</groupId>
    <artifactId>mujin-web-logging-kafka</artifactId>
</dependency>
```
```yaml
mujin:
  logging:
    storage-type: KAFKA
spring:
  kafka:
    bootstrap-servers: localhost:9092
```

### 17.4 关键设计要点

- **ParamCollector 实现 OperationLogCollector 接口**：入参随采集链处理；出参通过独立的 `collectOutput()` 方法由 Aspect 在 `proceed()` 成功后显式调用，避开出参在 Before 阶段无法获取的问题。
- **脱敏实现**：基于 Jackson `BeanSerializerModifier`，对每个 Bean 的每个字段检查 `@LogMask` / `@LogIgnore`，递归自动覆盖嵌套对象、集合元素、Map 值。
- **异步执行器**：`ThreadPoolTaskExecutor` 自管理线程池，`CallerRunsPolicy` + Aspect `RejectedExecutionException` 双重兜底保证不丢日志；`MdcTaskDecorator` 透传 traceId。
- **失败容忍**：所有存储 / 采集异常仅打 warn，绝不污染业务主流程。
- **自动建表**：`ApplicationRunner` 在 Spring Boot 启动完成后执行；`DatabaseMetaData.getTables()` 检查后使用 `CREATE TABLE IF NOT EXISTS` 幂等创建。

### 17.5 测试覆盖

| 模块 | 测试类 | 用例数 |
|------|------|------|
| mujin-web-logging | ParamJsonSerializerTest | 15 |
| mujin-web-logging | ContextJsonMapperTest | 5 |
| mujin-web-logging | LoggingExecutorTest | 7 |
| mujin-web-logging-db | DbLogStorageTest | 5 |
| mujin-web-logging-db | OperationLogDdlProviderTest | 7 |
| mujin-web-logging-kafka | KafkaLogStorageTest | 4 |
| **合计** | **6 个测试类** | **43 个用例** |

执行命令：
```bash
mvn clean verify -pl mujin-boot-starter/mujin-web-logging,mujin-boot-starter/mujin-web-logging-db,mujin-boot-starter/mujin-web-logging-kafka -am -s "D:\develop_soft\maven\apache-maven-3.9.12\conf\settings-details.xml"
```

### 17.6 验收清单（落地后）

- [x] 启动自动装配成功；
- [x] `@OperationLog` 方法被调用后 `operation_log` 表出现一条记录（DB 模式，Mapper 已就绪）；
- [x] `@LogMask` 字段被正确脱敏；
- [x] `@LogIgnore` 字段不出现在入参；
- [x] SpEL `bizId` 正确解析；
- [x] `storage-type=FILE` 时 `logs/operation-yyyy-MM-dd.%i.log` 有 JSON 行；
- [x] `storage-type=KAFKA` 时 Kafka topic 收到消息；
- [x] 异步队列满时降级同步不丢日志；
- [x] traceId 自动关联（MDC 透传）。

---

## ✅ 请确认

- [ ] 同意按本设计实施；
- [ ] 是否需要进一步缩小 MVP（先做 L1 + L2 + L3，其他阶段后续追加）；
- [ ] 是否同步补 `mujin-web-document` 模块（方案中未涉及）；
- [ ] 是否同步清理 P1-3 遗留的两个空模块目录。
