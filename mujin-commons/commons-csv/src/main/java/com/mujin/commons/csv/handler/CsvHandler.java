package com.mujin.commons.csv.handler;


import com.mujin.commons.csv.enums.CsvHandlerEnum;

/**
 * csv处理顶级接口
 *
 * @author chenglin.wu
 */
public interface CsvHandler {
    /**
     * 获取处理的枚举类型
     *
     * @return CsvHandlerEnum
     * @date 2025/11/23
     */
    CsvHandlerEnum getHandlerType();
}
