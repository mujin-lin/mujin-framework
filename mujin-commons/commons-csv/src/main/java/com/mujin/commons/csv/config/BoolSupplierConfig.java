package com.mujin.commons.csv.config;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * boolean写出获取对应的配置
 *
 * @author chenglin.wu
 */
@Data
@NoArgsConstructor
public class BoolSupplierConfig {
    /**
     * 当boolean值为 true时的数据
     */
    private String trueValue;
    /**
     * 当boolean值为 false时的数据
     */
    private String falseValue;
    /**
     * 当boolean值为 默认数据
     */
    private String defaultValue;

    public BoolSupplierConfig(String trueValue, String falseValue, String defaultValue) {
        this.trueValue = trueValue;
        this.falseValue = falseValue;
        this.defaultValue = defaultValue;
    }
}
