package com.warehouse.common;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果封装。
 *
 * @param <T> 列表元素类型
 */
@Data
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 总记录数 */
    private Long total;
    /** 当前页数据 */
    private List<T> records;

    public PageResult() {
    }

    public PageResult(Long total, List<T> records) {
        this.total = total;
        this.records = records;
    }

    public static <T> PageResult<T> of(Long total, List<T> records) {
        return new PageResult<>(total, records);
    }
}
