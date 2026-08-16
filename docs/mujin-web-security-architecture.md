# mujin-web-security 架构与使用说明

> 基于 Spring MVC `HandlerInterceptor` 的校验器链模块。
> 提供可插拔的 `SecurityValidator`，支持登录校验、限流、自定义业务校验。

---

## 1. 模块定位

`mujin-web-security` 不是完整的安全框架（如 Spring Security），而是：

1. **轻量级校验器链** —— 业务方按需注册 `SecurityValidator`，按 order 顺序串联
2. **请求体多次读取** —— `wrapper-enable=true` 时将 `HttpServletRequest` 包装为可多次读 body 的 wrapper
3. **可插拔开关** —— `validator-enable=true` 才生效，关闭则仅提供 wrapper 能力

---

## 2. 包结构

```
com.mujin.security/
├── constants/          SecurityConfigurationConstants
├── properties/         MjSecurityRequestProperties
├── interceptor/        ValidatorInterceptor
├── validator/          SecurityValidator / SecurityValidatorChain
│                       SecurityValidatorConfigurer / SecurityValidatorRegistry / SecurityValidatorRegistration
│   └── context/        PreHandleValidatorContext / AfterValidatorContext / AfterHandlerValidatorContext / ValidatorContext
└── ValidatorAutoConfiguration.java
```

---

## 3. 启用方式

### 3.1 引入依赖

```xml
<dependency>
    <groupId>com.mujin.boot</groupId>
    <artifactId>mujin-web-security</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### 3.2 application.yml

```yaml
mujin:
  web:
    config:
      request:
        security:
          wrapper-enable: true           # 是否包装 HttpServletRequest 为可多次读 body
          validator-enable: true         # 是否启用校验器链
```

### 3.3 启动类配置

```java
@SpringBootApplication
@EnableSecurityValidator
public class Application {

    @Bean
    public SecurityValidatorConfigurer validatorConfigurer() {
        return registry -> registry.add(new LoginValidator(), 100)        // order=100 先执行
                                .add(new RateLimitValidator(), 200);    // order=200 后执行
    }
}
```

---

## 4. 关键接口

### 4.1 `SecurityValidator`

```java
public interface SecurityValidator {
    void validateBefore(PreHandleValidatorContext context);   // 请求到达前
    void validateAfter(AfterHandlerValidatorContext context); // 请求结束后（资源清理）
}
```

### 4.2 `SecurityValidatorConfigurer`

```java
public interface SecurityValidatorConfigurer {
    void registryValidator(SecurityValidatorRegistry registry);
}
```

### 4.3 `SecurityValidatorRegistry`

```java
public class SecurityValidatorRegistry {
    public SecurityValidatorRegistry add(SecurityValidator validator, int order);
}
```

---

## 5. 自定义校验器

### 5.1 登录校验器

```java
public class LoginValidator implements SecurityValidator {

    @Override
    public void validateBefore(PreHandleValidatorContext ctx) {
        HttpServletRequest request = ctx.getRequest();
        if (!LoginContextHolder.isLoggedIn(request)) {
            throw new BusinessException(AuthorizationErrorCode.NOT_LOGGED_IN, "请先登录");
        }
    }

    @Override
    public void validateAfter(AfterHandlerValidatorContext ctx) {
        // 请求结束后清理资源（如清理 ThreadLocal）
    }
}
```

### 5.2 限流校验器

```java
public class RateLimitValidator implements SecurityValidator {

    private final LoadingCache<String, AtomicInteger> counter = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofSeconds(5))
            .maximumSize(100_000)
            .build(k -> new AtomicInteger(0));

    @Override
    public void validateBefore(PreHandleValidatorContext ctx) {
        String ip = WebUtil.getIp(ctx.getRequest());
        int count = counter.get(ip).incrementAndGet();
        if (count > 10) {
            throw new BusinessException(AuthorizationErrorCode.TOO_MANY_REQUESTS, "访问过于频繁");
        }
    }
}
```

---

## 6. 请求体多次读取

当 `wrapper-enable=true` 时，框架将 `HttpServletRequest` 包装为 `CachedBodyHttpServletRequest`，允许：

```java
@PostMapping
public ResponseResult<?> create(HttpServletRequest request, @RequestBody UserDTO dto) {
    // 第一次读取（@RequestBody 解析）
    // ...
    // 第二次读取（filter/interceptor 读取）
    String body = IoUtil.read(request.getInputStream(), StandardCharsets.UTF_8);
    return ResponseUtils.success();
}
```

适用于：操作日志采集请求体、Web 上下文记录、Sentinel 等需要二次解析的场景。

---

## 7. 执行顺序

```
HTTP Request
  └── Filter
       └── ValidatorInterceptor (order = Integer.MIN_VALUE，最先执行)
            ├── wrapper (可选)
            └── SecurityValidatorChain
                 ├── validator1 (order=100)
                 ├── validator2 (order=200)
                 └── validator3 (order=300)
            └── Controller
       └── ValidatorInterceptor.validateAfter() (请求结束后逆序调用)
            └── SecurityValidatorChain.validateAfter()
                 ├── validator3
                 ├── validator2
                 └── validator1
```

---

## 8. 关键 API

| 类型 | 路径 | 说明 |
| --- | --- | --- |
| 接口 | `com.mujin.security.validator.SecurityValidator` | 校验器接口 |
| 接口 | `com.mujin.security.validator.SecurityValidatorConfigurer` | 注册器配置 |
| 类 | `com.mujin.security.validator.SecurityValidatorRegistry` | 注册器 |
| 类 | `com.mujin.security.validator.SecurityValidatorRegistration` | 注册条目（validator + order） |
| 类 | `com.mujin.security.validator.SecurityValidatorChain` | 校验器链（链式调用） |
| 类 | `com.mujin.security.interceptor.ValidatorInterceptor` | Spring MVC 拦截器 |

---

## 9. 常见问题

**Q1：自定义校验器如何控制顺序？**  
注册时指定 order：`registry.add(myValidator, 100)`，order 越小越先执行。

**Q2：`validateAfter` 在什么场景使用？**  
资源清理（ThreadLocal / Context 清理）、耗时统计、慢请求日志等。

**Q3：能否替换框架默认的登录校验器？**  
框架**不**提供默认登录校验器——业务方完全自定义 `SecurityValidator`。

**Q4：校验器抛出异常后还会执行 `validateAfter` 吗？**  
会。框架保证 `validateAfter` 总是被调用（类似 try-finally）。

**Q5：wrapper 启用后会影响性能吗？**  
会轻微增加内存占用（缓存整个 body）。建议仅在需要多次读取 body 的接口使用，或在 Controller 维度单独启用。
