package com.github.util.structure.iterator.partition;

import java.util.List;

/**
 * @author X1993
 * @date 2023/4/27
 * @description
 */
@FunctionalInterface
public interface XPartitionFunction<T> {

    /**
     * 查询下一个分区
     * @param prePartitionLastElement 上个分区最后一个元素
     * @return
     */
    List<T> select(T prePartitionLastElement);

}
