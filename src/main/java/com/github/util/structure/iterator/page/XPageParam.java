package com.github.util.structure.iterator.page;

/**
 * 分页 入参
 * @author X1993
 * @date 2023/4/25
 * @description
 */
public interface XPageParam {

    /**
     * 分页下标，从1开始
     * @return
     */
    int pageIndex();

    /**
     * 分页大小
     * @return > 0
     */
    int pageSize();

}
