# commons-csv 架构与使用说明

> CSV 序列化与反序列化模块，**无 Spring 依赖**。
> 支持对象 ↔ CSV 双向转换、`@CsvProperty` 自定义列名与顺序、`@CsvIgnore` 排除字段、`@CsvGenerics` 处理集合。

---

## 1. 模块定位

`commons-csv` 是独立的 CSV 处理工具库：

1. **零依赖** —— 不依赖 Spring / Spring Boot，仅 JDK 21 + 反射
2. **注解驱动** —— `@CsvProperty` / `@CsvIgnore` / `@CsvGenerics` / `@CsvDateFormat`
3. **流式 API** —— `CsvOperateUtil.read/write/writeString` 静态方法
4. **错误码体系** —— `CsvErrorEnum` 实现 `ErrorCodeDefinition`

---

## 2. 包结构

```
com.mujin.commons.csv/
├── annotations/         CsvProperty / CsvIgnore / CsvGenerics / CsvDateFormat
├── config/              BoolSupplierConfig / CsvHandlerConfig
├── constants/           CsvHandlerConstants
├── enums/               CsvHandlerEnum / CsvErrorEnum
├── exception/           CsvException / CsvReadException / CsvWriteException
├── factory/             CsvHandlerFactory
├── handler/             CsvHandler（接口）
│                       CsvBasicAbstractHandler / CsvDateAbstractHandler / CsvJsonAbstractHandler / CsvOtherAbstractHandler
│                       CsvCollectionAbstractHandler / CsvReadHandler / CsvWriteHandler
│   ├── read/            BasicCsvReader / DateCsvReader / JsonFormatterCsvReader / OtherCsvReader / CollectionCsvReader
│   └── write/           BasicCsvWrite / DateCsvWrite / JsonFormatterCsvWrite / OtherCsvWrite / CollectionCsvWrite
├── entry/               FieldCacheEntry
└── CsvOperateUtil.java
```

---

## 3. 引入依赖

```xml
<dependency>
    <groupId>com.mujin.commons</groupId>
    <artifactId>commons-csv</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

---

## 4. 注解

### 4.1 `@CsvProperty`

字段级注解，控制表头、顺序、JSON 格式化与自定义 getter/setter：

```java
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CsvProperty {
    String value() default "";           // 列名（默认使用字段名）
    int index() default -1;              // 列顺序（-1=按声明顺序）
    boolean formatJson() default false;  // 是否将对象序列化为 JSON 字符串
    String dataInvokeMethod() default "";// 自定义 getter 方法名
    String dataSetInvokeMethod() default ""; // 自定义 setter 方法名
}
```

### 4.2 `@CsvIgnore`

```java
@CsvIgnore
private String password;                // 不写入 / 不读取
```

### 4.3 `@CsvGenerics`

集合 / 泛型字段，指定内部元素类型：

```java
@CsvGenerics(value = AddressDTO.class)
private List<AddressDTO> addresses;
```

### 4.4 `@CsvDateFormat`

日期字段格式化：

```java
@CsvDateFormat(pattern = "yyyy-MM-dd HH:mm:ss")
private LocalDateTime createTime;
```

---

## 5. 关键 API

### 5.1 `CsvOperateUtil`

| 方法 | 用途 |
| --- | --- |
| `read(File, Class<T>, CsvHandlerConfig)` | 读取 CSV 到对象集合 |
| `read(InputStream, Class<T>, CsvHandlerConfig)` | 同上 |
| `read(String, Class<T>, CsvHandlerConfig)` | 同上（CSV 字符串） |
| `write(File, Collection<T>, Class<T>, CsvHandlerConfig)` | 写出到文件 |
| `writeString(Collection<T>, Class<T>, CsvHandlerConfig)` | 返回 CSV 字符串 |
| `write(Collection<T>, Class<T>, CsvHandlerConfig)` | 返回字节数组 |

### 5.2 `CsvHandlerConfig`

```java
@Data
public class CsvHandlerConfig {
    private String charset = "UTF-8";    // 编码
    private String delimiter = ",";      // 分隔符
    private String lineSeparator = "\n"; // 行分隔符
    private boolean withBom = false;     // 是否写入 BOM（Excel 兼容）
}
```

---

## 6. 使用示例

### 6.1 定义模型

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

    @CsvGenerics(value = AddressDTO.class)
    @CsvProperty(value = "地址列表", index = 4, formatJson = true)
    private List<AddressDTO> addresses;

    public String formatToString() {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(createTime);
    }
}
```

### 6.2 写出 CSV

```java
List<UserExport> users = userService.findAll();
String csv = CsvOperateUtil.writeString(users, UserExport.class, null);
// 或：
byte[] bytes = CsvOperateUtil.write(users, UserExport.class, null);
Files.write(Paths.get("users.csv"), bytes);
```

### 6.3 读取 CSV

```java
List<UserExport> users = CsvOperateUtil.read(new File("users.csv"), UserExport.class, null);
```

### 6.4 写入文件 + BOM（Excel 兼容）

```java
CsvHandlerConfig config = new CsvHandlerConfig();
config.setWithBom(true);

CsvOperateUtil.write(new File("users.csv"), users, UserExport.class, config);
```

---

## 7. 异常体系

| 异常 | 错误码 | 说明 |
| --- | --- | --- |
| `CsvException` | - | CSV 处理通用异常 |
| `CsvReadException` | - | 读取失败 |
| `CsvWriteException` | - | 写入失败 |

`CsvErrorEnum`：

| 错误码 | 说明 |
| --- | --- |
| `READ_FAILED(7001)` | 读取失败 |
| `WRITE_FAILED(7002)` | 写入失败 |
| `INVALID_CONFIG(7003)` | 配置无效 |

---

## 8. 关键 API 总览

| 类型 | 路径 | 说明 |
| --- | --- | --- |
| 工具 | `com.mujin.commons.csv.CsvOperateUtil` | CSV 读写入口 |
| 注解 | `com.mujin.commons.csv.annotations.CsvProperty` | 列定义 |
| 注解 | `com.mujin.commons.csv.annotations.CsvIgnore` | 忽略字段 |
| 注解 | `com.mujin.commons.csv.annotations.CsvGenerics` | 集合泛型 |
| 注解 | `com.mujin.commons.csv.annotations.CsvDateFormat` | 日期格式 |
| 配置 | `com.mujin.commons.csv.config.CsvHandlerConfig` | CSV 处理器配置 |
| 异常 | `com.mujin.commons.csv.exception.CsvException` | CSV 通用异常 |
| 枚举 | `com.mujin.commons.csv.enums.CsvErrorEnum` | CSV 错误码 |

---

## 9. 常见问题

**Q1：CSV 中中文乱码？**  
设置 `CsvHandlerConfig.charset = "UTF-8"`；如果是 Excel 打开 CSV，推荐开启 `withBom = true`。

**Q2：如何处理集合字段？**  
使用 `@CsvGenerics` 指定集合元素类型，并结合 `@CsvProperty(formatJson = true)` 序列化为 JSON 字符串。

**Q3：日期格式怎么自定义？**  
在字段上标注 `@CsvDateFormat(pattern = "...")`。

**Q4：字段名与 CSV 列名不一致？**  
`@CsvProperty(value = "用户ID")` 显式指定列名。

**Q5：如何忽略某个字段？**  
`@CsvIgnore` 标注字段即可，读取和写入都不会处理。

**Q6：能处理嵌套对象吗？**  
支持简单嵌套。复杂对象建议序列化为 JSON 字符串（`formatJson = true`）或拆分为多个独立字段。
