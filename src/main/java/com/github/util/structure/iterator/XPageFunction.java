package com.github.util.structure.iterator;

/**
 * 分页查询函数
 * @author X1993
 * @date 2023/4/25
 * @description
 */
@FunctionalInterface
public interface XPageFunction<T> {

    XPageResult<T> select(XPageParam param);

}
