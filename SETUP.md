# 框架配置与代码风格

本目录汇总项目级别的代码风格与编辑器配置，建议所有 IDE 都加载一次，确保团队风格一致。

## 文件清单

| 文件 / 目录                                | 说明                                                                                       |
|--------------------------------------------|--------------------------------------------------------------------------------------------|
| `CLAUDE.md`                                | 项目结构、技术栈、模块职责与开发约定                                                        |
| `checkstyle.xml`                           | Checkstyle 主规则（命名、长度、缩进、Javadoc、import 等）                                   |
| `suppressions.xml`                         | Checkstyle 抑制规则（如 Lombok 生成方法、annotation 类）                                    |
| `.editorconfig`                            | 跨编辑器（IDEA / VSCode / Eclipse）的缩进、编码、行尾统一                                   |
| `.idea/codeStyles/Project.xml`             | IDEA 项目级 Code Style：缩进 4、行宽 180、注解换行                                          |
| `.idea/fileTemplates/internal/*`           | IDEA 新建文件模板：Class / Model / Enum / Annotation / AutoConfiguration                    |
| `.idea/templates/mujin-framework.xml`      | IDEA Live Template：`/**`/`*cm`/`*cf`/`log` 一键生成 Javadoc 与 Logger 字段                  |
| `CONTRIBUTING.md`                          | 贡献指南：提交规范、分支模型、PR 流程                                                       |

## 在 IDEA 中启用

1. **加载 Code Style**：`File → Settings → Editor → Code Style → Scheme → Import Scheme → IntelliJ IDEA Code Style XML`，选择 `.idea/codeStyles/Project.xml`。
2. **加载 Live Template**：`File → Settings → Editor → Live Templates`，点击右上角齿轮 → `Import Templates from File`，选择 `.idea/templates/mujin-framework.xml`。
3. **加载 File Template**：复制 `.idea/fileTemplates/internal/*` 至 `${idea.config}/fileTemplates/internal/`（macOS 在 `~/Library/Application Support/JetBrains/<IdeaVersion>/fileTemplates/internal`）。
4. **启用 Checkstyle**：
   - 安装 `Checkstyle-IDEA` 插件；
   - 在 `Settings → Tools → Checkstyle` 中新增配置，指向仓库根目录的 `checkstyle.xml`。

## 在 VSCode 中启用

- 安装扩展：`EditorConfig for VS Code`、`Checkstyle for Java`（或 `Checkstyle`）；
- VSCode 会自动读取仓库根目录的 `.editorconfig`；
- 在 `settings.json` 中加入：
  ```json
  "java.format.settings.url": "${workspaceFolder}/.idea/codeStyles/Project.xml"
  ```

## 在 Eclipse 中启用

- 安装 `Checkstyle Plugin`，使用相同 `checkstyle.xml`；
- 使用 `Window → Preferences → General → Workspace → New Text File Line Delimiter → Other → Windows` 同步 CRLF。

## 版本管理

- 修改任一风格文件需要走 PR，并附上 **前后对比** 截图或 diff；
- 修改 Checkstyle 规则后必须同步 `checkstyle.xml`、`suppressions.xml` 以及项目内的 JavaDoc 示例。
