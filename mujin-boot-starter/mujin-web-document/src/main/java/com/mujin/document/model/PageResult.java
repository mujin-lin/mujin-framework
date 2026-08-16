package com.mujin.document.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 分页结果模型
 *
 * @author chenglin.wu
 * @date 2026/08/16
 */
@Data
public class PageResult<T> {

    /**
     * 当前页（从 0 开始）
     */
    private int page;

    /**
     * 每页大小
     */
    private int size;

    /**
     * 总记录数
     */
    private long total;

    /**
     * 当前页数据
     */
    private List<T> items = new ArrayList<>();

    /**
     * 创建分页结果
     *
     * @param page  当前页
     * @param size  每页大小
     * @param total 总记录数
     * @param items 当前页数据
     * @return PageResult<T> 分页结果
     * @author chenglin.wu
     * @date 2026/08/16
     */
    public static <T> PageResult<T> of(int page, int size, long total, List<T> items) {
        PageResult<T> result = new PageResult<>();
        result.setPage(page);
        result.setSize(size);
        result.setTotal(total);
        result.setItems(items == null ? new ArrayList<>() : items);
        return result;
    }
}
