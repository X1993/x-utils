package com.github.util.structure.iterator.partition;

/**
 * @author X1993
 * @date 2023/4/27
 * @description
 */
@FunctionalInterface
public interface XPartitionFunction<T> {

    /**
     * 查询下一个分区
     * @param preElement 上一个遍历的元素
     * @return
     */
    Iterable<T> select(T preElement);

}
