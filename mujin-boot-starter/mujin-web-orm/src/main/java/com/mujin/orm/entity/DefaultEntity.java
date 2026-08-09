package com.mujin.orm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 系统默认的实体， 包含默认的字段<br/>
 * id 、create_by、update_by、create_time、update_time、update_time
 *
 * @author chenglin.wu
 * @date 2025/12/27
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class DefaultEntity extends BaseEntity<Long> {

    /**
     * 创建人
     */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private String createBy;
    /**
     * 修改人
     */
    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private String updateBy;
    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    /**
     * 修改时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    /**
     * 删除标注
     */
    @TableLogic(value = "0", delval = "1")
    @TableField(value = "del_flag", fill = FieldFill.INSERT)
    private Integer delFlag;

}
