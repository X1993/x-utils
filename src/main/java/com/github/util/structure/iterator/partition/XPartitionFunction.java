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
     * @param prePartitionList 上一个分区查询结果
     * @return 下一个分区查询结果
     */
    List<T> select(List<T> prePartitionList);

}
