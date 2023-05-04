package com.github.util.structure.iterator.partition;

import com.github.util.structure.iterator.XIterator;
import java.util.*;

/**
 * 内部基于分区实现的迭代器
 * @author X1993
 * @date 2023/4/27
 * @description
 */
public class XPartitionIterator<T> implements XIterator<T> {

    private Iterator<T> partitionIterator = Collections.EMPTY_LIST.iterator();

    private final XPartitionFunction<T> partitionFunction;

    private boolean finished;

    private T preElement;

    public XPartitionIterator(XPartitionFunction<T> partitionFunction) {
        Objects.requireNonNull(partitionFunction);
        this.partitionFunction = partitionFunction;
    }

    @Override
    public boolean hasNext()
    {
        while (true) {
            if (partitionIterator.hasNext()) {
                return true;
            }
            //跳到下一个分区
            if (finished) {
                return false;
            }

            partitionIterator = partitionFunction.select(preElement).iterator();
            if (!partitionIterator.hasNext()) {
                //没有更多数据了
                finished = true;
                return false;
            }
        }
    }

    @Override
    public T next() {
        return preElement = partitionIterator.next();
    }

}
