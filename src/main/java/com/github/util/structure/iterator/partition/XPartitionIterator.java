package com.github.util.structure.iterator.partition;

import com.github.util.structure.iterator.XIterator;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * 内部基于分区实现的迭代器
 * @author X1993
 * @date 2023/4/27
 * @description
 */
public class XPartitionIterator<T ,P> implements XIterator<T> {

    private Iterator<T> partitionIterator;

    private final XPartitionFunction<T ,P> partitionFunction;

    private boolean finished;

    private XPartitionFunction.Output<T ,P> preOutput;

    public XPartitionIterator(XPartitionFunction<T ,P> partitionFunction) {
        Objects.requireNonNull(partitionFunction);
        this.partitionFunction = partitionFunction;
        //加载第一个分区
        this.preOutput = partitionFunction.selectFirst();
        List<T> partition = this.preOutput.getPartition();
        this.partitionIterator = (partition != null ? partition : Collections.EMPTY_LIST).iterator();
    }

    @Override
    public boolean hasNext()
    {
        while (true) {
            if (partitionIterator.hasNext()) {
                return true;
            }

            if (finished) {
                return false;
            }

            if (!preOutput.isHasNext()) {
                finished = true;
                return false;
            }

            //加载下一个分区
            XPartitionFunction.Output<T ,P> output = partitionFunction.select(new XPartitionFunction.Input<T ,P>()
                    .setCurrentParam(preOutput.getNextParam())
                    .setPrePartition(preOutput.getPartition()));

            if (output == null){
                throw new IllegalStateException("分区加载函数返回值不允许为null");
            }
            preOutput = output;

            List<T> partition = preOutput.getPartition();
            partitionIterator = (partition != null ? partition : Collections.EMPTY_LIST).iterator();
        }
    }

    @Override
    public T next() {
        return partitionIterator.next();
    }

}
