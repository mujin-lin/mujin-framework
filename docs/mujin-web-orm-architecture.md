# mujin-web-orm 架构与使用说明

> 基于 MyBatis-Plus 3.5.15 的 ORM 增强模块。
> 提供 `BaseEntity` / `DefaultEntity`、自动填充、`@SearchColumn` 动态查询、分页插件、乐观锁、防全表更新。

---

## 1. 模块定位

`mujin-web-orm` 不重新封装 MyBatis-Plus，而是在它之上提供：

1. **实体基类** —— `BaseEntity<ID>` / `DefaultEntity`（含 createBy/updateBy/createTime/updateTime/delFlag）
2. **自动填充机制** —— `@EnableAutoFill` 注解扫描自定义 `InsertFillColumnHandler` / `UpdateFillColumnHandler`
3. **动态查询构造器** —— `@SearchColumn` 注解 + `SearchBase` 基类
4. **分页 DTO** —— `PageDto<T>` / `SearchPageDto` / `EntityPageDto`
5. **MyBatis-Plus 插件链** —— 分页 / 乐观锁 / 防全表更新（按配置开关）

---

## 2. 包结构

```
com.mujin.orm/
├── annotations/         EnableAutoFill / SearchColumn
├── configuration/       MjOrmConfig / OrmAutoConfiguration / MybatisPlusMetaHandler
├── conditional/         AutoFillCondition
├── constants/           OrmConfigurationConstants
├── dto/                 SearchBase / PageDto / PageExtra / SearchPageDto / EntityPageDto / AutoFillDto
├── entity/              BaseEntity / DefaultEntity
├── handler/             FillSupplier / InsertFillColumnHandler / UpdateFillColumnHandler
│   └── impl/            CreteTimeFillHandler / UpdateTimeFillHandler / DelFlagFillHandler
├── utils/               ReflectionUtils
├── AutoFillComponentSelector.java
├── AutoFillRegister.java
└── MybatisPlusInterceptorBuilder（内部类）
```

---

## 3. 启用方式

### 3.1 引入依赖

```xml
<dependency>
    <groupId>com.mujin.boot</groupId>
    <artifactId>mujin-web-orm</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
</dependency>
```

### 3.2 application.yml

```yaml
mujin:
  orm:
    enabled: true                              # 模块总开关

  web:
    config:
      orm:
        open-page-interceptor: true             # 分页插件（默认 true）
        optimistic-locker: false                # 乐观锁插件
        block-attack-inner: false               # 防全表更新/删除插件
        enable-auto-fill: true                  # 是否启用自动填充扫描
```

### 3.3 启动类注解

```java
@SpringBootApplication
@EnableAutoFill(basePackages = "com.example.app.entity")  // 扫描自定义填充处理器
public class Application { }
```

---

## 4. 实体基类

### 4.1 `BaseEntity<ID>` —— 最简基类

```java
@Data
public abstract class BaseEntity<ID extends Serializable> implements Serializable {
    public abstract ID getId();
    public abstract void setId(ID id);
}
```

### 4.2 `DefaultEntity` —— 含审计字段 + 逻辑删除

```java
@Data
@EqualsAndHashCode(callSuper = true)
public class User extends DefaultEntity {
    @TableId
    private Long id;
    private String account;
    private String userName;
}
```

`DefaultEntity` 自带字段：

| 字段 | 类型 | 说明 | 自动填充 |
| --- | --- | --- | --- |
| `createBy` | String | 创建人 | ✅ |
| `updateBy` | String | 更新人 | ✅ |
| `createTime` | LocalDateTime | 创建时间 | ✅ |
| `updateTime` | LocalDateTime | 更新时间 | ✅ |
| `delFlag` | Integer | 逻辑删除标记（0=未删，1=已删） | ✅ |

> 逻辑删除需在 `application.yml` 中配置 `mybatis-plus.global-config.db-config.logic-delete-field=delFlag`。

---

## 5. 自动填充

### 5.1 自定义 `InsertFillColumnHandler`

```java
@Component
public class MyInsertFillHandler implements InsertFillColumnHandler {

    @Override
    public FillSupplier insertFill() {
        return metaObject -> {
            // 从登录上下文取用户
            metaObject.setValue("createBy", LoginContextHolder.currentUserName());
        };
    }

    @Override
    public int getInsertFillOrder() {
        return 100;  // order 越小越先执行
    }
}
```

### 5.2 自定义 `UpdateFillColumnHandler`

```java
@Component
public class MyUpdateFillHandler implements UpdateFillColumnHandler {

    @Override
    public FillSupplier updateFill() {
        return metaObject -> metaObject.setValue("updateBy", LoginContextHolder.currentUserName());
    }

    @Override
    public int getUpdateFillOrder() {
        return 100;
    }
}
```

### 5.3 框架内置填充器

| 类 | 字段 | 说明 |
| --- | --- | --- |
| `CreteTimeFillHandler` | `createTime` | 创建时间（LocalDateTime.now()） |
| `UpdateTimeFillHandler` | `updateTime` | 更新时间（每次 update 自动填充） |
| `DelFlagFillHandler` | `delFlag` | 逻辑删除标记（insert 时默认 0） |

通过 `@EnableAutoFill(enableFrameworkFill = false)` 可禁用框架默认填充器。

---

## 6. 动态查询（@SearchColumn）

### 6.1 定义 Search DTO

```java
@Data
public class UserSearchDto extends SearchBase {

    @SearchColumn("user_name")               // 映射到数据库列名 user_name
    private String userName;

    @SearchColumn(exist = false)              // exist=false 表示 NOT LIKE（默认 LIKE）
    private String accountNotLike;

    @Override
    public <T extends BaseEntity> T getWrapper() {
        return null;                          // 可选：返回额外 wrapper
    }
}
```

### 6.2 在 Service 中使用

```java
@Service
public class UserService extends ServiceImpl<UserMapper, User> {

    public Page<User> search(UserSearchDto dto) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(dto.getUserName()),
                     User::getUserName, dto.getUserName());
        return page(new Page<>(dto.getPageNum(), dto.getPageSize()), wrapper);
    }
}
```

---

## 7. 分页 DTO

### 7.1 `PageDto<T>`

```java
@Data
public class PageDto<T> {
    private long pageNum = 1;
    private long pageSize = 10;
    private long total;
    private List<T> records;
    private PageExtra extra;                  // 扩展字段
}
```

### 7.2 `SearchPageDto`

继承 `PageDto` 并携带 `SearchBase`，用于搜索 + 分页一体：

```java
@Data
public class UserSearchPageDto extends SearchPageDto<User> {
    @SearchColumn("user_name")
    private String userName;
}
```

### 7.3 `EntityPageDto<Entity>`

继承 `PageDto` 并携带实体类型，用于按实体分页：

```java
@Data
public class UserPageDto extends EntityPageDto<User> {
    private String userName;                  // 业务字段
}
```

---

## 8. MyBatis-Plus 插件链

通过 `MjOrmConfig`（由 `OrmAutoConfiguration` 注册）控制插件启用：

| 插件 | 配置 | 默认 |
| --- | --- | --- |
| `PaginationInnerInterceptor` | `open-page-interceptor` | ✅ |
| `OptimisticLockerInnerInterceptor` | `optimistic-locker` | ❌ |
| `BlockAttackInnerInterceptor` | `block-attack-inner` | ❌ |

```yaml
mujin:
  web:
    config:
      orm:
        open-page-interceptor: true
        optimistic-locker: true
        block-attack-inner: true
```

---

## 9. 关键 API

| 类型 | 路径 | 说明 |
| --- | --- | --- |
| 注解 | `com.mujin.orm.annotations.EnableAutoFill` | 启用自动填充扫描 |
| 注解 | `com.mujin.orm.annotations.SearchColumn` | 字段映射到列名 |
| 实体 | `com.mujin.orm.entity.BaseEntity` | 实体基类 |
| 实体 | `com.mujin.orm.entity.DefaultEntity` | 默认实体（含审计） |
| DTO | `com.mujin.orm.dto.SearchBase` | 搜索基类 |
| DTO | `com.mujin.orm.dto.PageDto` | 分页 DTO |
| DTO | `com.mujin.orm.dto.SearchPageDto` | 搜索 + 分页 DTO |
| DTO | `com.mujin.orm.dto.EntityPageDto` | 实体 + 分页 DTO |
| 接口 | `com.mujin.orm.handler.InsertFillColumnHandler` | 插入填充处理器 |
| 接口 | `com.mujin.orm.handler.UpdateFillColumnHandler` | 更新填充处理器 |
| 函数 | `com.mujin.orm.handler.FillSupplier` | 填充回调 |

---

## 10. 使用示例

### 10.1 完整示例

```java
// 实体
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_user")
public class User extends DefaultEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String account;
    private String userName;
}

// Mapper
public interface UserMapper extends BaseMapper<User> { }

// Service
@Service
public class UserService extends ServiceImpl<UserMapper, User> {
    public boolean create(User user) {
        return save(user);  // 自动填充 createBy/createTime/delFlag
    }

    public boolean modify(User user) {
        return updateById(user);  // 自动填充 updateBy/updateTime
    }

    public Page<User> search(UserSearchDto dto) {
        return page(PageDto.toPage(dto),
                    SearchColumnHelper.toWrapper(dto));
    }
}

// Controller
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping
    public boolean create(@RequestBody User user) {
        return userService.create(user);
    }

    @PostMapping("/search")
    public Page<User> search(@RequestBody UserSearchDto dto) {
        return userService.search(dto);
    }
}
```

---

## 11. 常见问题

**Q1：`@EnableAutoFill` 必须加吗？**  
仅当业务需要自定义 `InsertFillColumnHandler` / `UpdateFillColumnHandler` 时才加。框架默认填充器（createTime/updateTime/delFlag）由 `OrmAutoConfiguration` 自动注册。

**Q2：`DefaultEntity` 的 `delFlag` 字段怎么生效？**  
需在 `application.yml` 配置 `mybatis-plus.global-config.db-config.logic-delete-field=delFlag`，
且数据库表 `del_flag` 字段类型为 `tinyint` / `int`。

**Q3：乐观锁怎么用？**  
在实体字段上标注 `@Version`，并在 `application.yml` 启用 `optimistic-locker: true`。

**Q4：分页插件不生效？**  
检查 `open-page-interceptor: true` 且 Mapper 继承 `BaseMapper`。
