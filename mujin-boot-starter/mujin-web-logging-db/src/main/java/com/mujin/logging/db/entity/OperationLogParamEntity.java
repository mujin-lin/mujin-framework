package com.mujin.logging.db.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 操作日志参数明细表实体（对应表 {@code ${table-prefix}operation_log_param}）
 * <p>
 * 通过 {@code logId} 关联 {@link OperationLogEntity}，每条入参 / 出参独立成行。
 *
 * @author chenglin.wu
 * @date 2026/08/09
 */
@Data
@TableName("operation_log_param")
@SuppressWarnings("unused")
public class OperationLogParamEntity {

    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联 {@link OperationLogEntity#getId()}
     */
    private Long logId;

    /**
     * 参数方向：IN / OUT
     */
    private String paramType;

    /**
     * 参数顺序
     */
    private Integer paramIndex;

    /**
     * 参数名
     */
    private String paramName;

    /**
     * 参数值（JSON / 字符串）
     */
    private String paramValue;
}
