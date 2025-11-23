package com.mujin.commons.csv.config;

import lombok.Data;

import java.nio.charset.Charset;

/**
 * csv 处理数据的配置类
 *
 * @author chenglin.wu
 */
@Data
public class CsvHandlerConfig {
    /**
     * 读取的字符编码集
     */
    private Charset charset;
    /**
     * 分割符号
     */
    private String delimiter;
    /**
     * header 行号 文件中显示的行号
     */
    private int headerLine;
    /**
     * 数据起始行号，文件中显示的行号
     */
    private int dataStartLine;

}
