package com.github.util.structure.iterator.page;

/**
 * 分页查询函数
 * @author X1993
 * @date 2023/4/25
 * @description
 */
@FunctionalInterface
public interface XPageFunction<T> {

    /**
     * 根据分页参数查询
     * @param param
     * @return
     */
    XPageResult<T> select(XPageParam param);

}
