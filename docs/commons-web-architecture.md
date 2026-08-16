# commons-web 架构与使用说明

> Web 通用模块（**可选 Spring 依赖**），提供注解、请求包装、响应模型、登录用户管理、IP 工具等。

---

## 1. 模块定位

`commons-web` 提供 Web 层通用基础设施：

1. **统一响应** —— `ResponseResult<T>` + `ResponseUtils`（success / fail）
2. **注解** —— `@LoginCheck`（登录校验）/ `@AccessLimit`（访问限流）
3. **登录用户管理** —— `LoginUserManager` 接口 + `LoginUserModel` 模型
4. **请求包装** —— `MjHttpRequestWrapper`（可多次读 body）
5. **配置属性** —— `CommonsProperties` / `CorsConfigProperties` / `RequestInfoPrintProperties`

> 该模块**不**包含 Spring 自动装配类，所有 Bean 注册由 `mujin-web-boot-starter` 完成。

---

## 2. 包结构

```
com.mujin.commons.web/
├── annotations/         LoginCheck / AccessLimit
├── configuration/       CommonsProperties / CorsConfigProperties / RequestInfoPrintProperties
├── constants/           RequestConstants / ReflectConstants
├── enums/               CommonsConfigEnum / DataError / RequestClientError
├── handler/             RequestInstance / RequestDealHandler / HttpServletRequestHandler / ServerRequestHandler
├── manager/             LoginUserManager（接口）
├── model/               LoginUserModel
├── request/             MjHttpRequestWrapper
├── response/            ResponseResult / BaseResponseResult / ResponseUtils
└── utils/               ValidatorUtils / TokenUtil / RequestHeaderFactory / IpUtils
```

---

## 3. 引入依赖

```xml
<dependency>
    <groupId>com.mujin.commons</groupId>
    <artifactId>commons-web</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

> 该模块仅依赖 `commons-lang` 和 Spring Web（可选）。如不需要 Spring 注解与 `HttpServletRequest` 相关类，可仅引入 `commons-lang`。

---

## 4. 统一响应

### 4.1 `ResponseResult<T>`

```java
@Data
public class ResponseResult<T> implements Serializable {
    private int resCode;          // 响应码，0 = 成功
    private String resMsg;        // 响应信息
    private T resData;            // 响应数据
}
```

### 4.2 `ResponseUtils`

```java
ResponseUtils.success();                      // success + null
ResponseUtils.success(data);                  // success + data
ResponseUtils.success(data, "操作成功");
ResponseUtils.fail(500, "服务器异常");
ResponseUtils.fail(errCodeDefinition, "业务异常");
ResponseUtils.fail(500, "操作失败", "详细错误信息");
```

### 4.3 Controller 使用

```java
@RestController
@RequestMapping("/order")
public class OrderController {

    @PostMapping
    public ResponseResult<OrderDTO> create(@RequestBody OrderCreateReq req) {
        OrderDTO order = orderService.create(req);
        return ResponseUtils.success(order);
    }

    @GetMapping("/{id}")
    public ResponseResult<OrderDTO> get(@PathVariable Long id) {
        return ResponseUtils.success(orderService.getById(id));
    }
}
```

---

## 5. 注解

### 5.1 `@LoginCheck`

标记需要登录态的 Controller / 方法：

```java
@RestController
@RequestMapping("/order")
@LoginCheck                                       // 类级：所有方法需登录
public class OrderController {

    @GetMapping("/list")
    public ResponseResult<List<OrderDTO>> list() { ... }

    @PostMapping
    public ResponseResult<OrderDTO> create(@RequestBody OrderCreateReq req) { ... }

    @LoginCheck(false)                            // 方法级：覆盖类级，设置无需登录
    @GetMapping("/public")
    public ResponseResult<?> publicEndpoint() { ... }
}
```

> `@LoginCheck` 仅作为**标记**，实际登录校验由业务方注册的 `SecurityValidator` 完成（详见 `mujin-web-security`）。

### 5.2 `@AccessLimit`

访问限流（默认 3 秒内最多 10 次，超出禁用 10 秒）：

```java
@PostMapping("/sms/send")
@AccessLimit(value = 60, maxVisits = 1, forbiddenSecond = 60)   // 60 秒内最多 1 次，禁用 60 秒
public ResponseResult<?> sendSms(@RequestBody SmsReq req) {
    smsService.send(req.getPhone());
    return ResponseUtils.success();
}
```

| 属性 | 默认 | 说明 |
| --- | --- | --- |
| `value` / `second` | 3L | 时间窗口（秒） |
| `maxVisits` | 10L | 时间窗口内最大访问次数 |
| `forbiddenSecond` | 10L | 超出后禁用时长（秒） |

> `@AccessLimit` 仅作为**标记**，实际限流由业务方注册的 `SecurityValidator` 完成。

---

## 6. 登录用户管理

### 6.1 `LoginUserModel` 接口

```java
public interface LoginUserModel {
    String getAccount();
    String getUserName();
    Long getId();
    String getToken();
    // ... 业务可扩展
}
```

### 6.2 `LoginUserManager` 接口

```java
public interface LoginUserManager<T extends LoginUserModel> {
    void setUser(HttpServletRequest request, T user);
    T getUser(HttpServletRequest request);
    void clear(HttpServletRequest request);
}
```

### 6.3 内置实现

`mujin-web-cache` 提供 `RedisLoginUserManager`：

```java
@Autowired
private LoginUserManager<LoginUserModel> loginUserManager;

public void login(HttpServletRequest req, String account) {
    LoginUserModel user = userService.findByAccount(account);
    loginUserManager.setUser(req, user);
}

public LoginUserModel currentUser(HttpServletRequest req) {
    return loginUserManager.getUser(req);
}
```

业务可自定义 `LoginUserManager`（如基于 Session / JWT / OAuth2）。

---

## 7. 请求包装

### 7.1 `MjHttpRequestWrapper`

将 `HttpServletRequest` 包装为可多次读取 body：

```java
HttpServletRequest wrapped = new MjHttpRequestWrapper(request);

// 第一次读取
byte[] body1 = StreamUtils.copyToByteArray(wrapped.getInputStream());

// 第二次读取（仍然能读到）
byte[] body2 = StreamUtils.copyToByteArray(wrapped.getInputStream());
```

### 7.2 启用方式

由 `mujin-web-security` 模块根据 `mujin.web.config.request.security.wrapper-enable=true` 自动包装。

---

## 8. 配置属性

### 8.1 `CommonsProperties`（`mujin.web.config.commons`）

| 字段 | 类型 | 默认 | 说明 |
| --- | --- | --- | --- |
| `encryptManagerType` | `CommonsConfigEnum` | `DEFAULT` | 加密管理器类型（DEFAULT / REDIS） |
| `loginUserManagerType` | `CommonsConfigEnum` | `DEFAULT` | 登录用户管理器类型（DEFAULT / REDIS） |

### 8.2 `CorsConfigProperties`（`mujin.web.config.cors`）

| 字段 | 类型 | 默认 | 说明 |
| --- | --- | --- | --- |
| `enableCors` | boolean | false | 是否开启 CORS |
| `mappingPathPattern` | String | `/**` | 跨域路径 |
| `allowedOriginPatterns` | String[] | `["*"]` | 允许的 origin |
| `allowedHeaders` | String[] | 常见请求头 | 允许的 header |
| `allowedMethods` | String[] | GET/POST/DELETE/PUT/OPTIONS | 允许的方法 |
| `maxAge` | long | 1800000 | 预检缓存时间（ms） |
| `allowCredentials` | boolean | true | 允许携带 cookie |

### 8.3 `RequestInfoPrintProperties`（`mujin.web.config.request`）

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `printUri` | boolean | 是否打印 URI |
| `printRequestIp` | boolean | 是否打印请求 IP |
| `printRequestOs` | boolean | 是否打印操作系统 |
| `printRequestSource` | boolean | 是否打印请求来源 |
| `printRequestBrowser` | boolean | 是否打印浏览器 |
| `printRequestParam` | boolean | 是否打印请求参数 |
| `printRequestBody` | boolean | 是否打印请求 body |

---

## 9. 工具类

| 工具 | 用途 |
| --- | --- |
| `IpUtils` | 获取客户端 IP（处理反向代理） |
| `TokenUtil` | Token 生成与解析 |
| `RequestHeaderFactory` | 请求头工厂 |
| `ValidatorUtils` | JSR-303 校验工具 |
| `RequestInstance` | 请求上下文访问 |
| `RequestDealHandler` | 请求处理基类 |
| `HttpServletRequestHandler` | HttpServletRequest 处理器 |
| `ServerRequestHandler` | WebFlux ServerRequest 处理器 |

---

## 10. 关键 API 总览

| 类型 | 路径 | 说明 |
| --- | --- | --- |
| 注解 | `com.mujin.commons.web.annotations.LoginCheck` | 登录校验标记 |
| 注解 | `com.mujin.commons.web.annotations.AccessLimit` | 访问限流标记 |
| 模型 | `com.mujin.commons.web.response.ResponseResult` | 统一响应 |
| 工具 | `com.mujin.commons.web.response.ResponseUtils` | 响应快捷构造 |
| 接口 | `com.mujin.commons.web.manager.LoginUserManager` | 登录用户管理 |
| 接口 | `com.mujin.commons.web.model.LoginUserModel` | 登录用户模型 |
| 类 | `com.mujin.commons.web.request.MjHttpRequestWrapper` | 请求包装器 |
| 类 | `com.mujin.commons.web.utils.IpUtils` | IP 工具 |
| 类 | `com.mujin.commons.web.utils.TokenUtil` | Token 工具 |

---

## 11. 常见问题

**Q1：`ResponseResult` 与 Spring MVC 的 `ResponseEntity` 怎么选择？**  
- `ResponseResult`：业务正常返回（成功 / 业务错误）  
- `ResponseEntity`：需要精细控制 HTTP 状态码、响应头时使用（如 201 Created + Location header）

**Q2：`@LoginCheck` 不生效？**  
框架仅提供**标记**，需要在 `mujin-web-security` 模块注册 `LoginValidator` 实现真正的登录校验。

**Q3：登录用户管理想用 Session 而不是 Redis？**  
实现 `LoginUserManager<LoginUserModel>` 接口，标注 `@Primary` 覆盖框架默认的 `RedisLoginUserManager`。

**Q4：`MjHttpRequestWrapper` 必须用吗？**  
仅在需要**多次读取请求 body** 时使用（如同时记录操作日志 + 入参校验）。否则直接用原始 `HttpServletRequest` 即可。
