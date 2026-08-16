# commons-lang 架构与使用说明

> 通用基础工具模块（**无 Spring 依赖**），可被任意 Java 项目直接引用。
> 提供时间、JSON、正则、AES 加解密、异常基类、错误码等基础设施。

---

## 1. 模块定位

`commons-lang` 是整个框架的基石，定位为：

1. **零 Spring 依赖** —— 不依赖任何 Spring / Spring Boot API
2. **纯 JDK 工具** —— 基于 JDK 21 + Jackson + Hutool 实现
3. **统一异常基类** —— `CommonsException` / `BusinessException` / `FrameworkException` + `ErrorCodeDefinition` 接口
4. **错误码体系** —— 按业务域段划分（用户 1001-1099、订单 2001-2099…）

---

## 2. 包结构

```
com.mujin.commons.lang/
├── code/                ErrorCodeDefinition（接口）/ BaseErrorCode / BusinessErrorCode / ServiceErrorCode / FrameworkErrorCode / DataCheckErrorCode / AuthorizationErrorCode
├── constants/           BaseDataTypeConstants / DateConstants / IntConstants
├── exception/           CommonsException / BusinessException / FrameworkException
├── jsonserial/          CustomDateSerializer / CustomTimeSerializer / DateDeserializer
├── model/               DeclaredAndSuperClass（反射工具模型）
├── DateTimeUtils.java
├── EncryptUtils.java
├── JsonUtil.java
└── RegexUtils.java
```

---

## 3. 引入依赖

```xml
<dependency>
    <groupId>com.mujin.commons</groupId>
    <artifactId>commons-lang</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

---

## 4. 关键 API

### 4.1 JSON（`JsonUtil`）

```java
JsonUtil.toJson(obj);                            // 对象 → JSON 字符串
JsonUtil.toObject(json, Class<T>);               // JSON → 对象
JsonUtil.toObject(json, new TypeReference<List<UserDTO>>(){});  // JSON → 集合
JsonUtil.toJsonNode(json);                       // JSON → JsonNode
JsonUtil.jsonMapper();                           // 获取 JsonMapper 实例
```

**特性**：

- 内置 Java 8 时间模块（`LocalDateTime` / `LocalDate` / `LocalTime`）
- 自定义日期格式：`yyyy-MM-dd HH:mm:ss` / `yyyy-MM-dd`
- `Long` → `String` 序列化（防止前端精度丢失）
- 失败时抛 `CommonsException`

### 4.2 时间（`DateTimeUtils`）

```java
DateTimeUtils.objectToDate(Object);              // 多源时间解析（Long / String / Date）
DateTimeUtils.format(Date);                      // Date → String（默认格式）
DateTimeUtils.format(Date, "yyyy/MM/dd");        // 自定义格式
DateTimeUtils.parse(String);                     // String → Date
```

### 4.3 正则（`RegexUtils`）

```java
RegexUtils.isPhone(String);                      // 手机号
RegexUtils.isEmail(String);                      // 邮箱
RegexUtils.isIdCard(String);                     // 身份证
RegexUtils.isPositiveInteger(String);            // 正整数
RegexUtils.isUrl(String);                        // URL
```

### 4.4 加密（`EncryptUtils`）

```java
EncryptUtils.aesEncrypt(content, key);           // AES 加密
EncryptUtils.aesDecrypt(content, key);           // AES 解密
EncryptUtils.generateKey(algorithm);             // 生成密钥（AES / DES）
EncryptUtils.toSha1(str);                        // SHA-1
EncryptUtils.base64Encode(str);                  // Base64 编码
EncryptUtils.base64Decode(str);                  // Base64 解码
```

---

## 5. 异常体系

### 5.1 异常基类

```java
public class CommonsException extends RuntimeException {
    private final int errCode;
    private final String errMsg;
    // ...
}

public class BusinessException extends CommonsException {
    public BusinessException(String errMsg);
    public BusinessException(ErrorCodeDefinition errCode, String errMsg);
    public BusinessException(ErrorCodeDefinition errCode, String errMsg, Throwable cause);
}

public class FrameworkException extends CommonsException {
    // 同上
}
```

### 5.2 错误码接口

```java
public interface ErrorCodeDefinition {
    int errorCode();
}
```

### 5.3 业务异常使用

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

// 业务代码
throw new BusinessException(OrderErrorCode.ORDER_NOT_FOUND, "订单 " + id + " 不存在");
```

### 5.4 错误码段位规范

| 段位 | 模块 | 说明 |
| --- | --- | --- |
| 500 | 通用 | 服务器异常（BaseErrorCode） |
| 700 | 框架 | 框架内部异常（FrameworkErrorCode） |
| 900 | 业务 | 业务系统异常（BusinessErrorCode） |
| 1001-1099 | 用户域 | 用户模块错误码 |
| 2001-2099 | 订单域 | 订单模块错误码 |
| 6001-6099 | 文档域 | 文档模块错误码（DocumentErrorCode） |
| 9000 | 系统 | 系统崩溃（CRASH_ERROR） |

**禁止混用**：业务模块的错误码段位不可与基础段位冲突。

---

## 6. 自定义异常示例

```java
package com.example.order.exception;

/**
 * 订单模块业务异常
 *
 * @author chenglin.wu
 * @date 2026/08/16
 */
public class OrderException extends BusinessException {

    public OrderException(String errMsg) {
        super(errMsg);
    }

    public OrderException(ErrorCodeDefinition errCode, String errMsg) {
        super(errCode, errMsg);
    }

    public OrderException(ErrorCodeDefinition errCode, String errMsg, Throwable cause) {
        super(errCode, errMsg, cause);
    }
}
```

---

## 7. 关键 API 总览

| 类型 | 路径 | 说明 |
| --- | --- | --- |
| 工具 | `com.mujin.commons.lang.JsonUtil` | JSON 序列化 |
| 工具 | `com.mujin.commons.lang.DateTimeUtils` | 时间工具 |
| 工具 | `com.mujin.commons.lang.RegexUtils` | 正则校验 |
| 工具 | `com.mujin.commons.lang.EncryptUtils` | 加解密 |
| 接口 | `com.mujin.commons.lang.code.ErrorCodeDefinition` | 错误码接口 |
| 枚举 | `com.mujin.commons.lang.code.BaseErrorCode` | 基础错误码 |
| 枚举 | `com.mujin.commons.lang.code.BusinessErrorCode` | 业务错误码 |
| 枚举 | `com.mujin.commons.lang.code.ServiceErrorCode` | 服务错误码 |
| 枚举 | `com.mujin.commons.lang.code.FrameworkErrorCode` | 框架错误码 |
| 枚举 | `com.mujin.commons.lang.code.DataCheckErrorCode` | 数据校验错误码 |
| 枚举 | `com.mujin.commons.lang.code.AuthorizationErrorCode` | 鉴权错误码 |
| 异常 | `com.mujin.commons.lang.exception.CommonsException` | 通用异常基类 |
| 异常 | `com.mujin.commons.lang.exception.BusinessException` | 业务异常 |
| 异常 | `com.mujin.commons.lang.exception.FrameworkException` | 框架异常 |

---

## 8. 常见问题

**Q1：commons-lang 能不依赖 Spring Boot 单独使用吗？**  
可以。该模块不依赖 Spring，可被任意 Java 项目（包括 Android、桌面应用）直接引用。

**Q2：业务异常必须继承 BusinessException 吗？**  
强烈建议。框架的全局异常处理器对 `BusinessException` / `FrameworkException` / `CommonsException` 有特殊处理（如返回特定的 HTTP 状态码、日志级别）。

**Q3：错误码段位可以重复吗？**  
不可以。不同业务模块的错误码段位必须唯一（如用户 1001-1099、订单 2001-2099），避免冲突。

**Q4：`JsonUtil` 与 Spring 默认的 Jackson 配置冲突吗？**  
不会。`JsonUtil` 使用独立的 `JsonMapper` 实例，不会污染 Spring 的全局 ObjectMapper。
