package com.github.util.structure.iterator.partition;

import java.util.*;

/**
 * 内部基于分区实现的迭代器
 * @author X1993
 * @date 2023/4/27
 * @description
 */
public class XPartitionIterator<T> implements Iterator<T> {

    private List<T> partitionResult = Collections.EMPTY_LIST;

    private final XPartitionFunction<T> partitionFunction;

    private int nextIndex;

    private boolean finished;

    public XPartitionIterator(XPartitionFunction<T> partitionFunction) {
        this.partitionFunction = partitionFunction;
    }

    @Override
    public boolean hasNext()
    {
        while (true) {
            if (nextIndex < partitionResult.size()) {
                return true;
            }
            //跳到下一个分区
            if (finished) {
                return false;
            }
            T prePartitionLastElement = partitionResult.isEmpty() ?
                    null : partitionResult.get(partitionResult.size() - 1);

            partitionResult = partitionFunction.select(prePartitionLastElement);
            nextIndex = 0;
            if (partitionResult == null || partitionResult.isEmpty()) {
                //没有更多数据了
                finished = true;
            }
        }
    }

    @Override
    public T next() {
        if (nextIndex < partitionResult.size()) {
            return partitionResult.get(nextIndex++);
        }
        throw new NoSuchElementException();
    }

}
