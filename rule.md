# mujin-framework 项目说明

## 项目概述

mujin-framework 是一个基于 **Spring Boot 3.5.x + JDK 21** 的 Java 企业级开发框架，采用 Maven 多模块结构组织代码，
提供通用工具、Web 增强、缓存、安全、ORM、日志、文档等开箱即用的能力。框架代码全部使用 **中文注释** 与 **中文错误信息**，
便于国内团队维护。

## 仓库地址

- Gitee: https://gitee.com/mujin/mujin-framework

## 父项目坐标

```xml
<groupId>com.mujin</groupId>
<artifactId>mujin-framework</artifactId>
<version>1.0.0-SNAPSHOT</version>
<packaging>pom</packaging>
```

## 模块结构

```
mujin-framework
├── mujin-commons/                  通用基础组件（无 Spring 强依赖）
│   ├── commons-lang/               时间/JSON/正则/加密/异常/错误码
│   ├── commons-csv/                CSV 序列化与反序列化
│   └── commons-web/                Web 通用：注解/请求/响应/校验（可选依赖 Spring）
└── mujin-boot-starter/             Spring Boot 自动装配组件
    ├── mujin-web-boot-starter/     框架入口自动配置（CommonsProperties、CORS、请求日志）
    ├── mujin-web-security/         安全相关扩展
    ├── mujin-web-cache/            缓存相关（Redis / 本地 ExpiringMap）
    ├── mujin-web-orm/              ORM 相关（基于 MyBatis-Plus）
    ├── mujin-web-logging/          日志相关
    ├── mujin-web-document/         接口文档相关
    └── mujin-web-model/            通用模型（DTO/VO 等）
```

每个 `module` 都遵循标准 Maven 结构：`src/main/java`、`src/main/resources`。
Spring Boot 自动装配类统一注册在 `src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`。

## 技术栈

| 维度         | 版本              | 备注                                  |
|--------------|-------------------|---------------------------------------|
| JDK          | 21                | 使用 `release` 编译，兼容 JDK 9+     |
| Spring Boot  | 3.5.8             | 父 BOM 统一管理                       |
| Lombok       | 1.18.42           | 必须使用，注解处理器已配置            |
| Hutool       | 5.8.41            | 工具库，引入 hutool-bom               |
| Jackson      | 2.19.2            | databind + jsr310                     |
| commons-lang3| 3.20.0            | Apache Commons Lang                   |
| MyBatis-Plus | 3.5.15            | ORM（仅 mujin-web-orm 引入）          |
| ExpiringMap  | 0.5.11            | 本地过期缓存                          |

## 构建命令

```bash
# 全量编译（包含 javadoc 与 sources 打包）
mvn clean install

# 跳过 javadoc 加速
mvn clean install -Dmaven.javadoc.skip=true

# 指定模块构建
mvn -pl mujin-commons/commons-lang -am clean install
```

## 关键约定

### 包名规范

| 模块                         | 根包前缀                   |
|------------------------------|----------------------------|
| mujin-commons/commons-lang   | `com.mujin.commons.lang`   |
| mujin-commons/commons-csv    | `com.mujin.commons.csv`    |
| mujin-commons/commons-web    | `com.mujin.commons.web`    |
| mujin-boot-starter/*         | `com.mujin.<业务子包>`     |

### 命名约定

- 类名：`UpperCamelCase`，接口不加 `I` 前缀，实现类可加后缀（如 `XxxHandler`、`XxxManager`）。
- 方法名：`lowerCamelCase`，布尔型用 `is/has/can` 前缀；判断型用 `check/validate`。
- 常量：`UPPER_SNAKE_CASE`。
- 包名：全小写、单词相连。
- 所有自定义注解统一放在 `annotations` 包，保持一致。
- Spring Bean：以业务语义命名（如 `redisCacheManager`、`simpleCacheManager`），不要带 `Impl` 后缀。

### 注释规范

- 类、方法、字段的 Javadoc 注释使用中文描述，遵循 `@author chenglin.wu`、`@date yyyy/MM/dd` 标签。
- 行内注释使用中文，关键逻辑必须说明意图，不要描述语法。
- 接口或抽象方法必须编写完整 Javadoc，含 `@param`、`@return`、`@date`。
- 私有方法可不写 Javadoc，但建议补充 `// 说明` 形式注释。

### 异常规范

- 业务异常使用 `commons-lang` 中的 `BusinessException`、`FrameworkException`、`CommonsException`。
- 错误码枚举实现 `ErrorCodeDefinition` 接口，统一存放在 `code` 包。
- 避免直接 `throw new RuntimeException(...)`。

### Lombok 规范

- 业务类首选 `@Data`；枚举常量字段使用 `@Getter` 或 `@RequiredArgsConstructor`。
- 不使用 `@Builder` 滥用，对象构造仍以静态工厂方法或构造器为准。
- 子模块 `<dependency>lombok</dependency>` 必须设置为 `<optional>true</optional>`。

### Spring 自动装配

- 新增 starter 必须：
  1. 编写 `XxxAutoConfiguration` 类并标注 `@Configuration`（必要时 `@ConditionalOnXxx`）。
  2. 在 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 中添加全限定类名。
  3. 使用 `@ConfigurationProperties` 绑定 `mujin.*` 前缀配置。

## 开发工作流

1. 新建模块：在 `mujin-commons` 或 `mujin-boot-starter` 下创建子模块，并在父 pom 中声明。
2. 新建工具类：在对应模块的 `<业务>` 包下创建 `XxxUtil` 终结类，构造方法私有。
3. 新建枚举：实现 `ErrorCodeDefinition` 或作为业务枚举，字段使用 `final` + 构造器注入。
4. 新建注解：放在 `annotations` 包，使用 `@Target`、`@Retention`、`@Documented`。
5. 新建配置：在 `configuration` 包下创建 `XxxProperties`，使用 Lombok `@Data`.

## 禁止事项

- 不要引入与父 BOM 重复或冲突的依赖版本。
- 不要在公共模块使用 `@Component`、`@Service` 等 Spring 注解，应由 starter 模块装配。
- 不要修改 `application.yml` 等运行时配置文件（仓库内不包含）。
- 不要将 `target/` 目录提交（已加入 `.gitignore` 思路，扩展时一并维护）。

## 相关文档

- [代码风格约束](checkstyle.xml) — Checkstyle 规则
- [编辑器风格](.editorconfig) — 跨 IDE 缩进/编码
- [贡献指南](CONTRIBUTING.md) — 提交规范与代码评审

---

## 代码示例（AI 编码必读）

> 本章节是 **AI 编码必须遵循的样例**。Claude / Cursor / Copilot 在 mujin-framework 任何子模块生成代码时，
> 必须严格按下述示例的命名、包结构、注释、异常、Lombok 用法编写，并复用 `commons-lang` 中的异常基类与错误码接口。

### 1. 通用工具类

```java
package com.mujin.commons.lang.utils;

/**
 * 字符串相关工具方法
 *
 * @author chenglin.wu
 * @date 2026/08/08
 */
@SuppressWarnings("unused")
public final class StringUtils {

    /**
     * 私有构造，禁止实例化
     */
    private StringUtils() {
    }

    /**
     * 判断字符串是否为空（null 或空串）
     *
     * @param str 待校验字符串
     * @return boolean true 表示为空
     * @author chenglin.wu
     * @date 2026/08/08
     */
    public static boolean isBlank(String str) {
        return str == null || str.isEmpty();
    }
}
```

要点：

- 类 `final` + 私有构造；
- 静态方法 + `@SuppressWarnings("unused")`；
- 中文 Javadoc，包含 `@param`、`@return`、`@author`、`@date`；
- 不依赖任何 Spring API，可放在 `mujin-commons` 任意子模块。

### 2. 错误码枚举

```java
package com.mujin.commons.lang.code;

/**
 * 用户模块错误码定义
 *
 * @author chenglin.wu
 * @date 2026/08/08
 */
public enum UserErrorCode implements ErrorCodeDefinition {
    /**
     * 用户不存在
     */
    USER_NOT_FOUND(1001),
    /**
     * 用户名重复
     */
    USER_NAME_DUPLICATE(1002),
    /**
     * 密码错误
     */
    USER_PASSWORD_ERROR(1003);

    /**
     * 错误码
     */
    private final int errorCode;

    UserErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public int errorCode() {
        return this.errorCode;
    }
}
```

要点：

- 实现 `ErrorCodeDefinition` 接口；
- 每个常量必须有中文注释；
- 字段 `final` + 构造器注入；
- 错误码按业务域段划分（如用户 `1001-1099`，订单 `2001-2099`）。

### 3. 业务异常

```java
package com.mujin.commons.lang.exception;

/**
 * 用户模块业务异常
 *
 * @author chenglin.wu
 * @date 2026/08/08
 */
@SuppressWarnings("unused")
public class UserException extends CommonsException {

    public UserException(String errMsg) {
        super(errMsg);
    }

    public UserException(ErrorCodeDefinition errCode, String errMsg) {
        super(errCode, errMsg);
    }

    public UserException(ErrorCodeDefinition errCode, String errMsg, Throwable cause) {
        super(errCode, errMsg, cause);
    }
}
```

要点：

- 继承 `CommonsException`；
- 至少提供「消息」「错误码 + 消息」「错误码 + 消息 + cause」三种构造器。

### 4. 注解

```java
package com.mujin.commons.web.annontations;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * 操作日志注解，打在 Controller 方法上用于记录操作日志
 *
 * @author chenglin.wu
 * @date 2026/08/08
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperateLog {

    /**
     * 操作描述
     *
     * @return String
     * @author chenglin.wu
     * @date 2026/08/08
     */
    String value() default "";

    /**
     * 是否保存请求参数
     *
     * @return boolean
     * @author chenglin.wu
     * @date 2026/08/08
     */
    boolean saveParam() default true;

    /**
     * 别名：操作描述，等价于 value
     *
     * @return String
     * @author chenglin.wu
     * @date 2026/08/08
     */
    @AliasFor("value")
    String description() default "";
}
```

要点：

- 放在 `annontations` 包（沿用项目历史拼写）；
- 必须声明 `@Target`、`@Retention`、`@Documented`；
- 每个属性都要 Javadoc + `@date`；
- 互为别名使用 `@AliasFor`。

### 5. 数据模型

```java
package com.mujin.boot.web.model;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应结果
 *
 * @author chenglin.wu
 * @date 2026/08/08
 */
@Data
@SuppressWarnings("unused")
public class ResponseResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 响应码，0 表示成功
     */
    private int resCode;

    /**
     * 响应信息
     */
    private String resMsg;

    /**
     * 响应数据
     */
    private T resData;
}
```

要点：

- 业务模型使用 `@Data`；
- 实现 `Serializable`，声明 `serialVersionUID`；
- 字段统一 `lowerCamelCase`，避免拼音或下划线。

### 6. 配置属性（Spring Boot）

```java
package com.mujin.boot.web.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Web 框架公共配置
 *
 * @author chenglin.wu
 * @date 2026/08/08
 */
@Data
@ConfigurationProperties(prefix = "mujin.web.config")
public class WebProperties {

    /**
     * 是否开启请求日志打印
     */
    private boolean enableRequestLog = true;

    /**
     * 是否开启 CORS
     */
    private boolean enableCors = false;
}
```

要点：

- `@ConfigurationProperties(prefix = "mujin.web.config")`；
- 配合 starter 的 `AutoConfiguration` 注入；
- 字段尽量给默认值，避免 NPE。

### 7. Spring Boot 自动装配

```java
package com.mujin.boot.web;

import com.mujin.boot.web.configuration.WebProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Web 通用自动配置
 *
 * @author chenglin.wu
 * @date 2026/08/08
 */
@Configuration
@EnableConfigurationProperties(WebProperties.class)
@ConditionalOnProperty(prefix = "mujin.web.config", name = "enable", matchIfMissing = true)
public class MujinWebAutoConfiguration {

    /**
     * 注册 WebProperties，缺失时自动装配
     *
     * @return WebProperties
     * @author chenglin.wu
     * @date 2026/08/08
     */
    @Bean
    @ConditionalOnMissingBean
    public WebProperties webProperties() {
        return new WebProperties();
    }
}
```

配套的 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`：

```
com.mujin.boot.web.MujinWebAutoConfiguration
```

要点：

- starter 类必须有完整 Javadoc；
- 配套在 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 中声明全限定类名；
- Bean 名称默认驼峰（`webProperties`），不使用 `Impl` 后缀。

### 8. AI 编码反面示例（禁止）

- ❌ 命名：`StringUtils`、`stringUtils`、`iUserService`；
- ❌ 注释：英文 Javadoc、缺失 `@param/@return`、仅一行注释；
- ❌ 异常：`throw new RuntimeException(...)`；
- ❌ 包名：`com.mujin.web.utils`（应放 `commons-web` 并使用 `utils` 子包），或在 starter 模块创建 `service` 包；
- ❌ Lombok：`@Data` 与 `@Builder` 同时使用导致无参构造器丢失；
- ❌ 工具类：未私有化构造器，可被 `new`；
- ❌ 自动装配：忘记在 `AutoConfiguration.imports` 注册；
- ❌ 公共模块：`@Component`、`@Service`、`@Autowired` 出现在 `mujin-commons`。

如违反上述规则，CI 的 `mvn checkstyle:check` 与人工评审均会驳回合并。

