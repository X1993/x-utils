package com.github.util.structure.iterator.partition;

import com.github.util.structure.iterator.XIterable;
import java.util.Iterator;

/**
 * @author X1993
 * @date 2023/4/27
 * @description
 */
public class XPartitionIterable<T> implements XIterable<T> {

    private XPartitionIterator<T> iterator;

    public XPartitionIterable(XPartitionFunction<T> partitionFunction){
        this.iterator = new XPartitionIterator<>(partitionFunction);
    }

    @Override
    public Iterator<T> iterator() {
        return iterator;
    }

}
