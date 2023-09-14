package com.github.util.structure.iterator.partition;

import com.github.util.structure.iterator.XIterable;
import java.util.Iterator;
import java.util.Objects;

/**
 * @author X1993
 * @date 2023/4/27
 * @description
 */
public class XPartitionIterable<T ,P> implements XIterable<T> {

    private final XPartitionFunction<T ,P> partitionFunction;

    public XPartitionIterable(XPartitionFunction<T ,P> partitionFunction){
        Objects.requireNonNull(partitionFunction);
        this.partitionFunction = partitionFunction;
    }

    @Override
    public Iterator<T> iterator() {
        return new XPartitionIterator<>(partitionFunction);
    }

}
