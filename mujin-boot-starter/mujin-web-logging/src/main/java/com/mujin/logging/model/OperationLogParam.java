package com.mujin.logging.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 操作日志参数项（入参/出参 通用）
 *
 * @author chenglin.wu
 * @date 2026/08/08
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OperationLogParam {

    /**
     * 参数方向：IN / OUT
     */
    private String paramType;

    /**
     * 参数顺序
     */
    private int paramIndex;

    /**
     * 参数名
     */
    private String paramName;

    /**
     * 参数值（JSON 或字符串）
     */
    private String paramValue;

    /**
     * 快捷构造：入参
     *
     * @param index 顺序
     * @param name  参数名
     * @param value 参数值
     * @return OperationLogParam
     */
    public static OperationLogParam ofIn(int index, String name, String value) {
        return new OperationLogParam("IN", index, name, value);
    }

    /**
     * 快捷构造：出参
     *
     * @param name  参数名（一般 "result"）
     * @param value 参数值
     * @return OperationLogParam
     */
    public static OperationLogParam ofOut(String name, String value) {
        return new OperationLogParam("OUT", 0, name, value);
    }
}
