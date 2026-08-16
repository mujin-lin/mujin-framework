package com.mujin.document.model;

import lombok.Data;

/**
 * 代码示例模型
 *
 * @author chenglin.wu
 * @date 2026/08/12
 */
@Data
public class CodeExample {

    /**
     * 语言标识：curl / java / python / javascript / go / php 等
     */
    private String language;

    /**
     * 示例标题
     */
    private String title;

    /**
     * 代码内容
     */
    private String code;

    /**
     * 示例描述
     */
    private String description;
}