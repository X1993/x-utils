package com.github.util.structure;

import java.util.Iterator;
import java.util.function.Function;

/**
 * 分区缓存迭代
 * 例：大表数据迭代，避免过程中消耗太多内存 （可参考单元测试）
 * @author X1993
 * @date 2023/2/3
 * @description
 */
public class PartitionCacheIterable<T> implements Iterable<T> {

    // Iterable要有序
    private final Function<T ,Iterable<T>> partitionQueryFunction;

    public PartitionCacheIterable(Function<T, Iterable<T>> partitionQueryFunction) {
        this.partitionQueryFunction = partitionQueryFunction;
    }

    /**
     * 分页缓存迭代
     * @param pageQueryFunction 函数入参是分页下标
     * @param <T>
     * @return
     */
    public static <T> PartitionCacheIterable<T> pageIterable(Function<Integer ,Iterable<T>> pageQueryFunction){
        return new PartitionCacheIterable<>(new Function<T, Iterable<T>>() {

            //分页下标
            int pageIndex = 0;

            @Override
            public Iterable<T> apply(T t) {
                return pageQueryFunction.apply(pageIndex++);
            }

        });
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>(){

            //当前缓存的分区迭代器，有序
            private Iterator<T> cacheIterator;

            //已遍历的最后一个元素
            private T last;

            @Override
            public boolean hasNext()
            {
                if (cacheIterator != null && cacheIterator.hasNext()) {
                    return true;
                }

                cacheIterator = partitionQueryFunction.apply(last).iterator();
                return cacheIterator.hasNext();
            }

            @Override
            public T next() {
                return last = cacheIterator.next();
            }
        };
    }
}
