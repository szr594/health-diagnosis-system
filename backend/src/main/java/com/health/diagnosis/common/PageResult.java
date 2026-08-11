package com.health.diagnosis.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果包装类。
 */
@Data
public class PageResult<T> implements Serializable {

    /** 总记录数 */
    private long total;

    /** 当前页数据 */
    private List<T> records;

    /** 当前页码 */
    private long pageNum;

    /** 每页条数 */
    private long pageSize;

    /**
     * 由 MyBatis-Plus 的分页对象转换。
     */
    public static <T> PageResult<T> of(IPage<T> page) {
        PageResult<T> result = new PageResult<>();
        result.setTotal(page.getTotal());
        result.setRecords(page.getRecords());
        result.setPageNum(page.getCurrent());
        result.setPageSize(page.getSize());
        return result;
    }

    /**
     * 手动构建分页结果（用于自定义联表查询）。
     */
    public static <T> PageResult<T> of(List<T> records, long total, long pageNum, long pageSize) {
        PageResult<T> result = new PageResult<>();
        result.setTotal(total);
        result.setRecords(records);
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        return result;
    }
}
