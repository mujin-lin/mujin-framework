# mujin-web-cache 架构与使用说明

> 基于 Spring Boot Cache + Redis + 本地 `ExpiringMap` 的缓存增强模块。
> 通过 `@EnableCacheCustomizer` 注解式注入自定义 `cacheName` / TTL / KeyPrefix / Serializer，
> 支持 SIMPLE / REDIS / MIX 三种模式。

---

## 1. 模块定位

`mujin-web-cache` 不是简单的 `@Cacheable` 封装，而是：

1. **按 cacheName 精细控制 TTL / KeyPrefix / Serializer** —— 通过 `RedisCacheManagerPrefixCaching` 接口
2. **支持运行时创建未声明的 cacheName** —— `allowRuntimeCreation` 开关
3. **SIMPLE 模式基于 Hutool `ExpiringMap`** —— 轻量级本地缓存，无需 Redis
4. **提供 `RedisLoginUserManager` 实现** —— 直接基于 `RedisCacheManager` 的登录用户缓存

---

## 2. 包结构

```
com.mujin.cache/
├── annotations/         EnableCacheCustomizer
├── caching/             CacheManagerCacheNameCaching / RedisCacheManagerPrefixCaching / SimpleCacheNameCaching
├── configuration/       （继承 Spring Cache 标准配置）
├── customizer/          CacheManagerBuilderCustomizer
├── enums/               CacheManagerEnum（SIMPLE / REDIS / MIX）
├── manager/             SimpleLocalExpireCache / SimpleLocalCacheManager / RedisLoginUserManager
├── register/            CacheCustomizerRegistry
├── scan/                RedisCacheNamePropertiesScanner / SimpleCacheNamePropertiesScanner
├── serializer/          CustomerJackson2JsonRedisSerializer
└── CacheManagerAutoConfiguration.java
```

---

## 3. 启用方式

### 3.1 引入依赖

```xml
<dependency>
    <groupId>com.mujin.boot</groupId>
    <artifactId>mujin-web-cache</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

### 3.2 启动类注解

```java
@SpringBootApplication
@EnableCacheCustomizer(
    basePackages = "com.example.app.cache",      // 扫描自定义 cacheName 配置
    cacheType = CacheManagerEnum.REDIS,           // SIMPLE / REDIS / MIX
    allowRuntimeCreation = true                   // 允许运行时创建未声明的 cacheName
)
public class Application { }
```

> `@EnableCacheCustomizer` 已经包含 `@EnableCaching`，**无需**再添加。

### 3.3 application.yml 配置

```yaml
mujin:
  cache:
    enabled: true                                # 必填，模块总开关

spring:
  cache:
    type: REDIS                                  # SIMPLE / REDIS / MIX
    cache-names: userCache, orderCache           # 预声明的缓存名
    redis:
      time-to-live: 10m                          # 默认 TTL（Duration 格式）
      key-prefix: "demo:"                        # 缓存 key 前缀
      use-key-prefix: true
      cache-null-values: false                   # 是否缓存 null
      enable-statistics: false                   # 是否开启统计
```

---

## 4. 自定义 cacheName 配置

实现 `RedisCacheManagerPrefixCaching`（REDIS 模式）或 `SimpleCacheNameCaching`（SIMPLE 模式）：

```java
@Component
public class UserCacheConfig implements RedisCacheManagerPrefixCaching {

    @Override
    public String cacheName() {
        return "userCache";
    }

    @Override
    public Duration expiry() {
        return Duration.ofMinutes(30);            // 该 cacheName 单独 TTL
    }

    @Override
    public String cachePrefix() {
        return "user:";                           // 覆盖全局 key-prefix
    }

    @Override
    public RedisSerializer<String> keySerializer() {
        return RedisSerializer.string();
    }

    @Override
    public RedisSerializer<?> valueSerializer() {
        return RedisSerializer.json();
    }
}
```

框架会在启动时扫描 `basePackages` 下所有实现类，自动注册为 `RedisCacheManagerBuilderCustomizer`。

---

## 5. 三种缓存模式对比

| 模式 | 适用场景 | 实现 |
| --- | --- | --- |
| `SIMPLE` | 单机本地缓存、临时缓存 | `ExpiringMap` + `SimpleLocalCacheManager` |
| `REDIS` | 分布式缓存、跨实例共享 | `RedisCacheManager`（框架增强版） |
| `MIX` | 一级本地 + 二级 Redis 兜底 | 两级缓存（业务自定义） |

---

## 6. 关键 API

| 类型 | 路径 | 说明 |
| --- | --- | --- |
| 注解 | `com.mujin.cache.annotations.EnableCacheCustomizer` | 启用缓存定制 |
| 接口 | `com.mujin.cache.caching.CacheManagerCacheNameCaching` | 缓存名配置通用接口 |
| 接口 | `com.mujin.cache.caching.RedisCacheManagerPrefixCaching` | Redis 模式 cacheName 定制 |
| 接口 | `com.mujin.cache.caching.SimpleCacheNameCaching` | SIMPLE 模式 cacheName 定制 |
| 实现 | `com.mujin.cache.manager.SimpleLocalExpireCache` | 本地 ExpiringMap 缓存实现 |
| 实现 | `com.mujin.cache.manager.RedisLoginUserManager` | 基于 RedisCacheManager 的登录用户管理 |

---

## 7. 使用示例

### 7.1 在 Service 中使用 `@Cacheable`

```java
@Service
public class UserService {

    @Cacheable(cacheNames = "userCache", key = "#userId")
    public UserDTO getById(Long userId) {
        return userMapper.selectById(userId);
    }

    @CacheEvict(cacheNames = "userCache", key = "#userId")
    public void update(Long userId, UserDTO dto) {
        userMapper.updateById(dto);
    }
}
```

### 7.2 多级缓存配置

```java
// 一级本地缓存（5 秒 TTL，进程内）
@Bean
public CacheManager localCacheManager() {
    SimpleCacheManager mgr = new SimpleCacheManager();
    // ...
    return mgr;
}

// 二级 Redis 缓存（30 分钟 TTL，跨进程）
@Bean
@Primary
public RedisCacheManager redisCacheManager() {
    // ...
}
```

---

## 8. 内置 LoginUserManager

框架基于 RedisCacheManager 提供 `RedisLoginUserManager` 实现，可直接 `@Autowired`：

```java
@Autowired
private LoginUserManager<LoginUserModel> loginUserManager;

public void login(HttpServletRequest req) {
    LoginUserModel user = new LoginUserModel(...);
    loginUserManager.setUser(req, user);
}

public LoginUserModel currentUser(HttpServletRequest req) {
    return loginUserManager.getUser(req);
}
```

---

## 9. 错误码与异常

- 模块本身不抛业务异常，所有错误透传 Spring Cache 原生异常。
- 配置错误（如 cacheName 重复）会在启动时 `WARN` 日志提示，不阻塞启动。

---

## 10. 测试与扩展

### 10.1 自定义 CacheManager

实现 `CacheManagerBuilderCustomizer` 接口并声明为 `@Bean`，框架会自动调用：

```java
@Bean
public CacheManagerBuilderCustomizer<RedisCacheManager.RedisCacheManagerBuilder> myCustomizer() {
    return builder -> builder.enableStatistics();
}
```

### 10.2 自定义 Serializer

```java
@Component
public class MySerializerConfig implements RedisCacheManagerPrefixCaching {
    // ...
}
```

---

## 11. 常见问题

**Q1：为什么要用 `@EnableCacheCustomizer` 而不是 Spring `@EnableCaching`？**  
框架注解内置 `@EnableCaching` + 自动扫描 + 精细 cacheName 控制，是 `@EnableCaching` 的超集。

**Q2：运行时使用未声明的 cacheName 会报错吗？**  
`allowRuntimeCreation=true` 时不会，使用默认配置（全局 TTL / KeyPrefix）创建。  
`allowRuntimeCreation=false` 时会抛 `IllegalArgumentException`。

**Q3：如何切换 SIMPLE / REDIS / MIX？**  
修改 `spring.cache.type` 配置；`@EnableCacheCustomizer.cacheType` 仅作为默认值，实际以 `spring.cache.type` 为准。

**Q4：自定义 cacheName 的优先级？**  
业务 `@Bean` > 框架 `RedisCacheManagerPrefixCaching` > Spring Boot 默认配置。
