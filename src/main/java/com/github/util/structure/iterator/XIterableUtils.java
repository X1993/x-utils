package com.github.util.structure.iterator;

import com.github.util.structure.iterator.limit.XLimitIterator;
import com.github.util.structure.iterator.page.XPageFunction;
import com.github.util.structure.iterator.page.XPageIterable;
import com.github.util.structure.iterator.partition.XPartitionFunction;
import com.github.util.structure.iterator.partition.XPartitionIterable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author wangjj7
 * @date 2023/5/30
 * @description
 */
public class XIterableUtils {

    /**
     * 限制最大迭代次数
     * @param iterator 原始迭代器
     * @param limit 最大迭代次数
     * @return
     */
    public static <T> XIterator<T> limit(Iterator<T> iterator ,int limit){
        return new XLimitIterator(iterator ,limit);
    }

    public static <T> XIterator<T> limit(Iterable<T> iterator ,int limit){
        return new XLimitIterator(iterator.iterator() ,limit);
    }

    /**
     * 根据指定大小分割成多个小迭代器
     * @param iterator 原始迭代器
     * @param splitSize 分割大小
     * @return
     */
    public static <T> Iterable<List<T>> split(Iterator<T> iterator, int splitSize){
        return () -> new Iterator<List<T>>() {

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public List<T> next() {
                List<T> list = new ArrayList<>(splitSize + 1);
                list.add(iterator.next());
                for (int i = 1; i < splitSize; i++) {
                    if (iterator.hasNext()){
                        list.add(iterator.next());
                    }else {
                        return list;
                    }
                }
                return list;
            }
        };
    }

    public static <T> Iterable<List<T>> split(Iterable<T> iterable, int splitSize){
        return split(iterable.iterator() ,splitSize);
    }

    public static <T> XPageIterable<T> pageIterable(XPageFunction<T> pageFunction){
        return new XPageIterable<>(pageFunction);
    }

    public static <T> XPartitionIterable<T> partitionIterable(XPartitionFunction<T> partitionFunction){
        return new XPartitionIterable<>(partitionFunction);
    }
    
}
