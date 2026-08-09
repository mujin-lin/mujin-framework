# 贡献指南

感谢你对 `mujin-framework` 感兴趣！在提交代码之前，请先阅读本文档，确保风格一致、流程顺畅。

## 1. 行为准则

- 所有参与者须遵守开源社区基本礼仪，友善沟通、客观评价。
- 禁止提交与框架无关的代码或敏感信息。

## 2. 提交前必读

- [CLAUDE.md](CLAUDE.md)：项目架构、技术栈、模块划分。
- [checkstyle.xml](checkstyle.xml) 与 [suppressions.xml](suppressions.xml)：强制代码风格。
- [.editorconfig](.editorconfig)：编辑器基础配置。

## 3. 分支模型

| 分支          | 用途                                        |
|---------------|---------------------------------------------|
| `master`      | 稳定版本，仅接受 hotfix / release           |
| `develop`     | 主开发分支，所有 PR 默认合入此处             |
| `feature/*`   | 新功能或重构（例：`feature/cache-prefix`）  |
| `hotfix/*`    | 紧急修复（例：`hotfix/20260808-csv-npe`）   |
| `release/*`   | 发布分支（例：`release/1.0.1`）             |

- Fork 仓库后基于 `develop` 拉取 `feature/*` 分支。
- 修复严重问题可从 `master` 拉取 `hotfix/*`。

## 4. 提交规范

提交信息遵循 **Conventional Commits**：

```
<type>(<scope>): <subject>

<body>

<footer>
```

### 4.1 type

| type       | 说明                                          |
|------------|-----------------------------------------------|
| `feat`     | 新增功能                                      |
| `fix`      | Bug 修复                                      |
| `docs`     | 文档注释                                      |
| `style`    | 不影响逻辑的格式调整（空格、格式化等）        |
| `refactor` | 重构（既不是新增功能，也不是修复 Bug）        |
| `perf`     | 性能优化                                      |
| `test`     | 增加或修改测试                                |
| `build`    | 构建系统或依赖更新                            |
| `ci`       | CI 相关                                       |
| `chore`    | 其他杂项                                      |

### 4.2 scope（可选）

填写子模块名，如 `commons-lang`、`web-cache`、`web-orm`。

### 4.3 示例

```
feat(web-cache): 支持自定义缓存名前缀动态刷新

- 新增 CacheNamePropertiesScanner 抽象类
- 默认实现 SimpleCacheNamePropertiesScanner

Closes #123
```

## 5. 代码风格

1. 所有源代码必须通过 `mvn checkstyle:check`（CI 强制）。
2. 缩进 4 空格、不使用 Tab、UTF-8 编码、CRLF 行尾。
3. 类、接口、枚举、注解必须编写中文 Javadoc。
4. 方法必须有完整 `@param`、`@return`。
5. 业务异常优先使用 `commons-lang` 提供的异常基类。
6. 不要在公共模块使用 Spring 注解（Spring 装配在 starter 中做）。

## 6. 测试规范

- 修改公共工具类必须补充单元测试（JUnit 5 + AssertJ）。
- 测试覆盖率不低于 `80%`（行覆盖），关键工具类要求 ≥ `90%`。
- 测试包路径与被测类保持一致，包名以 `.test` 结尾（仓库暂无该目录时，新建并声明）。

## 7. PR 流程

1. 完成代码后 `mvn clean verify -Dmaven.javadoc.skip=true` 自检。
2. 推送至远端 `feature/*` 分支。
3. 在 Gitee 上创建 Pull Request，填写：
   - 标题：与 commit 一致（或简单概括）。
   - 描述：变更内容、关联 Issue、影响范围、截图。
4. 至少 1 位 reviewer 通过、CI 全绿后方可合入。
5. 合入后删除源分支。

## 8. 版本号与发布

- 当前版本 `1.0.0-SNAPSHOT`。
- 发布前更新 `pom.xml` 中版本号，并在 `release` 分支打 tag（`v1.0.0`）。
- 发布步骤参见 [`docs/release.md`](SETUP.md)（如有）。

## 9. 安全问题

发现安全漏洞请**不要**通过公开 Issue 提交，邮件至 maintainer@example.com 或通过 Gitee 私信维护者。

## 10. License

贡献的代码遵循仓库根目录的 [LICENSE](LICENSE) 协议。
