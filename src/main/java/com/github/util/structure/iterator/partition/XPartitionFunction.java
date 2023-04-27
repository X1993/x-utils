package com.github.util.structure.iterator.partition;

import java.util.List;

/**
 * @author X1993
 * @date 2023/4/27
 * @description
 */
@FunctionalInterface
public interface XPartitionFunction<T> {

    List<T> select(List<T> prePartitionList);

}
