package com.mujin.orm.dto;


import com.mujin.commons.web.constants.ReflectConstants;
import lombok.Data;

/**
 * 分页扩展查询字段
 *
 * @author chenglin.wu
 * @date 2025/12/27
 */
@SuppressWarnings("unused")
@Data
public class PageExtra {
    /**
     * 范围查询的实体类字段名
     */
    private String columnName;

    /**
     * 左边区间的值
     */
    private Object beginValue;
    /**
     * 右边区间的值
     */
    private Object endValue;
    /**
     * 左边是否是关区间
     */
    private boolean closeLeft;
    /**
     * 右边是否是关区间
     */
    private boolean closeRight;

    /**
     * 判断是否是全闭合区间
     *
     * @return boolean
     */
    private boolean closeAllSection() {
        return this.closeLeft && this.closeRight;
    }

    private boolean openLeftAndCloseRight() {
        return !this.closeLeft && this.closeRight;
    }

    private boolean closeLeftAndOpenRight() {
        return this.closeLeft && !this.closeRight;
    }

    private boolean openAll() {
        return !this.closeLeft && !this.closeRight;
    }

    /**
     * 判断区间
     *
     * @return 区间的值
     */
    public String judgmentSection() {
        if (this.closeAllSection()) {
            return ReflectConstants.CLOSE_ALL;
        }
        if (this.openLeftAndCloseRight()) {
            return ReflectConstants.OPEN_LEFT_AND_CLOSE_RIGHT;
        }
        if (this.closeLeftAndOpenRight()) {
            return ReflectConstants.CLOSE_LEFT_AND_OPEN_RIGHT;
        }
        return ReflectConstants.OPEN_ALL;
    }

}
