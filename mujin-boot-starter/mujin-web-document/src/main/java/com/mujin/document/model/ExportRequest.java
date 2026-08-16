package com.mujin.document.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * API 文档导出请求参数
 *
 * @author chenglin.wu
 * @date 2026/08/12
 */
@Data
public class ExportRequest {

    /**
     * 导出格式：PDF / YAML / JSON / HTML
     */
    private String format = "PDF";

    /**
     * 分组名称列表，为空则导出所有
     */
    private List<String> groups = new ArrayList<>();

    /**
     * 标签列表，为空则导出所有
     */
    private List<String> tags = new ArrayList<>();

    /**
     * 是否包含调用示例
     */
    private boolean includeExamples = true;

    /**
     * 是否包含数据模型
     */
    private boolean includeModels = true;

    /**
     * 是否包含已弃用接口
     */
    private boolean includeDeprecated = false;

    /**
     * 语言列表（用于代码示例）
     */
    private List<String> languages = new ArrayList<>();

    /**
     * 纸张大小
     */
    private String pageSize = "A4";

    /**
     * 页面边距（mm）
     */
    private int margin = 20;

    /**
     * 自定义文件名（不含扩展名）
     */
    private String fileName;

    /**
     * 验证并设置默认值
     */
    public void validateAndSetDefaults() {
        if (languages == null || languages.isEmpty()) {
            languages.add("curl");
            languages.add("java");
            languages.add("python");
            languages.add("javascript");
        }
        if (fileName == null || fileName.isBlank()) {
            fileName = "api-document-" + System.currentTimeMillis();
        }
    }
}