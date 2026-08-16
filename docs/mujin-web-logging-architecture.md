# mujin-web-logging 架构与使用说明

> 通过 `@OperationLog` 注解自动采集方法入参/出参/异常/上下文，落地到 **DB / FILE / Kafka** 三种可插拔后端。
> 基于 Spring AOP + `@Async` + 自管理线程池，支持 `@LogMask` 字段脱敏、`@LogIgnore` 排除、SpEL 提取业务标识。

---

## 1. 模块定位

`mujin-web-logging` 提供「**零侵入式**」操作日志能力：

1. **注解驱动** —— `@OperationLog("保存订单")` 一行开启
2. **SpEL 提取** —— `bizId = "#req.orderId"`、`operator = "#userName"`
3. **递归脱敏** —— `@LogMask` 对嵌套对象 / 集合 / Map 元素自动生效
4. **异步非阻塞** —— 自管理线程池，写库不阻塞业务；队列满时降级同步（不丢日志）
5. **多后端可插拔** —— DB（MySQL）/ FILE（Logback JSON）/ Kafka（topic）
6. **独立数据源** —— 可选 `mujin.logging.datasource.*` 与业务库隔离
7. **MDC traceId 透传** —— 异步线程自动透传 traceId

---

## 2. 模块拆分

| 模块 | 后端 | 引入场景 |
| --- | --- | --- |
| `mujin-web-logging` | FILE（默认） | 仅文件落地，无需额外依赖 |
| `mujin-web-logging-db` | DB（MySQL） | 业务需落库 |
| `mujin-web-logging-kafka` | KAFKA | 业务需 Kafka 推送 |

---

## 3. 包结构

```
com.mujin.logging/
├── annotations/         OperationLog / LogMask / LogIgnore / MaskType
├── enums/               LogStorageType（DB / FILE / KAFKA）/ LogResultEnum（SUCCESS / FAIL）
├── configuration/       LoggingProperties
├── model/               OperationLogContext / OperationLogParam
├── aop/                 OperationLogAspect
├── collector/           OperationLogCollector / SpelParamCollector / ParamCollector / WebContextCollector / LoginUserCollector / DefaultLogContextCollector
├── context/             LoginContextHolder
├── serializer/          ParamJsonSerializer（含 @LogMask / @LogIgnore 处理）
├── persistence/         LogStorage / FileLogStorage / NoOpLogStorage / ContextJsonMapper
├── executor/            LoggingExecutor（异步线程池）
└── auto/                LoggingAutoConfiguration
```

---

## 4. 启用方式

### 4.1 引入依赖

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

### 4.2 application.yml

```yaml
mujin:
  logging:
    enabled: true                                  # 总开关（默认 false）
    storage-type: DB                               # DB / FILE / KAFKA
    async: true                                    # 是否异步写（推荐 true）
    thread-pool-size: 4                            # 异步线程池核心
    queue-capacity: 1024                           # 队列上限（满则降级同步）
    slow-threshold: 3000                           # 全局慢方法阈值（ms）
    capture-header: true                           # 是否记录请求头
    include-packages:                              # AOP 扫描包（留空=不限制）
      - "com.example.app.service"
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

---

## 5. 核心注解

### 5.1 `@OperationLog`（方法级）

```java
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {
    String value();                                // 操作描述（必填）
    String bizId() default "";                     // SpEL：业务对象标识
    String operator() default "";                  // SpEL：操作人（留空读登录上下文）
    boolean saveParam() default true;              // 是否保存入参
    boolean saveResult() default true;             // 是否保存出参
    long slowThreshold() default 3000L;            // 慢方法阈值（ms）
}
```

### 5.2 `@LogMask`（字段级脱敏）

```java
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogMask {
    MaskType value() default MaskType.KEEP_HEAD;   // 脱敏策略
    int head() default 3;                          // 保留头部 N 位
    int tail() default 4;                          // 保留尾部 N 位
}
```

`MaskType`：

| 策略 | 示例 | 说明 |
| --- | --- | --- |
| `KEEP_HEAD` | `138**********` | 保留头部 N 位 |
| `KEEP_TAIL` | `**********1234` | 保留尾部 N 位 |
| `MIDDLE` | `138****1234` | 头尾都保留 |
| `ALL` | `**********` | 全部替换 |

### 5.3 `@LogIgnore`（字段级不入参）

```java
@LogIgnore
private String password;
```

---

## 6. 业务示例

```java
@RestController
@RequestMapping("/order")
public class OrderController {

    @PostMapping
    @OperationLog(value = "创建订单", bizId = "#req.orderNo", slowThreshold = 500)
    public ResponseResult<OrderDTO> create(@RequestBody OrderCreateReq req) {
        return ResponseUtils.success(orderService.create(req));
    }
}

@Data
public class OrderCreateReq {
    @LogMask(MaskType.KEEP_HEAD)                       // 身份证保留头 3 位
    private String idCard;

    @LogMask(value = MaskType.MIDDLE, head = 3, tail = 4)   // 手机号保留 138****1234
    private String phone;

    @LogIgnore                                          // 不入参
    private String password;

    private String orderNo;
}
```

---

## 7. 关键设计

### 7.1 采集器链

`OperationLogAspect` 调用 `DefaultLogContextCollector`，后者按 `@Order` 顺序串联所有 `OperationLogCollector`：

```
OperationLogAspect
  └── DefaultLogContextCollector
        ├── SpelParamCollector        (SpEL 提取 bizId/operator)
        ├── ParamCollector            (入参序列化 + @LogMask/@LogIgnore)
        ├── WebContextCollector       (URI/IP/UA/Headers)
        ├── LoginUserCollector        (登录用户)
        └── CustomCollector           (业务自定义)
```

业务可通过 `@ConditionalOnMissingBean` 替换任一内置采集器，或新增 `@Component implements OperationLogCollector`。

### 7.2 异步执行器

- `@EnableAsync` + 自管理 `LoggingExecutor`（core=4 / queue=1024）
- 队列满时降级同步（`CallerRunsPolicy`）—— **不丢日志**
- `MdcTaskDecorator` 自动透传 MDC traceId 到异步线程

### 7.3 持久化抽象

`LogStorage` 接口：

```java
public interface LogStorage {
    void store(OperationLogEntity log) throws Exception;
}
```

- `FileLogStorage` —— Logback JSON 行输出（默认）
- DB 子模块提供 JDBC / MyBatis-Plus 实现
- Kafka 子模块提供 `KafkaTemplate` 实现
- 业务可自定义 `LogStorage` 替换

---

## 8. 三种后端启用

### 8.1 FILE 模式

无需额外依赖，引入 `mujin-web-logging` 即可。日志写入 `./logs/operation/operation.YYYY-MM-DD.log`。

### 8.2 DB 模式

```java
@SpringBootApplication
@MapperScan(basePackages = {
    "com.example.app.mapper",                          // 业务 Mapper
    "com.mujin.logging.db.mapper"                      // 操作日志 Mapper（复用业务数据源时必填）
})
public class Application { }
```

启动后自动建表：
- `mujin_operation_log`（主表）
- `mujin_operation_param`（参数表）

### 8.3 独立数据源

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

### 8.4 Kafka 模式

```yaml
mujin:
  logging:
    storage-type: KAFKA
spring:
  kafka:
    bootstrap-servers: localhost:9092
```

---

## 9. 关键 API

| 类型 | 路径 | 说明 |
| --- | --- | --- |
| 注解 | `com.mujin.logging.annotations.OperationLog` | 方法级触发日志织入 |
| 注解 | `com.mujin.logging.annotations.LogMask` | 字段级脱敏 |
| 注解 | `com.mujin.logging.annotations.LogIgnore` | 字段级不入参 |
| 枚举 | `com.mujin.logging.enums.LogStorageType` | DB / FILE / KAFKA |
| 枚举 | `com.mujin.logging.annotations.MaskType` | 脱敏策略 |
| 接口 | `com.mujin.logging.collector.OperationLogCollector` | 采集器接口 |
| 接口 | `com.mujin.logging.persistence.LogStorage` | 存储策略接口 |
| 类 | `com.mujin.logging.context.LoginContextHolder` | 登录用户上下文 |

---

## 10. 关键特性

- **异步执行**：默认异步写，队列满时降级同步执行（不丢日志）。
- **MDC 透传**：异步线程自动透传 traceId。
- **失败容忍**：写库 / Kafka 发送异常仅 warn，不污染业务。
- **自动建表**：DB 模式启动时自动创建两张表（幂等 `CREATE TABLE IF NOT EXISTS`）。
- **脱敏递归生效**：嵌套对象、集合元素、Map 值自动继承 `@LogMask` / `@LogIgnore`。

---

## 11. 常见问题

**Q1：如何关闭某个方法的操作日志？**  
不写 `@OperationLog` 注解即可；或全局 `mujin.logging.enabled=false` 关闭整个模块。

**Q2：`@LogMask` 对嵌套字段生效吗？**  
生效。`ParamJsonSerializer` 基于 Jackson `BeanSerializerModifier`，对每个 Bean 的字段递归检查注解。

**Q3：操作日志如何与现有 traceId 体系对接？**  
`WebContextCollector` 优先从请求头 `X-Trace-Id` 读取；异步线程通过 `MdcTaskDecorator` 自动透传 MDC。

**Q4：DB 模式下表已存在，自动建表会破坏数据吗？**  
不会。`OperationLogSchemaInitializer` 使用 `CREATE TABLE IF NOT EXISTS`，幂等。

**Q5：如何自定义采集器？**  
实现 `OperationLogCollector` 接口，标注 `@Component`，框架通过 `ObjectProvider` 自动发现并串联。

**Q6：如何自定义 LogStorage？**  
实现 `LogStorage` 接口，声明为 `@Bean`，框架通过 `@ConditionalOnMissingBean` 自动让位。
