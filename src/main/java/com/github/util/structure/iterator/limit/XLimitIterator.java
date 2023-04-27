package com.github.util.structure.iterator.limit;

import com.github.util.structure.iterator.XIterator;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * 限制最大迭代次数的迭代器
 * @author X1993
 * @date 2023/4/27
 * @description
 */
public class XLimitIterator<T> implements XIterator<T> {

    /**
     * 最大迭代数量
     */
    private final int limit;

    private int index;

    private final Iterator<T> iterator;

    public XLimitIterator(Iterator<T> iterator, int limit) {
        this.limit = limit;
        Objects.requireNonNull(iterator);
        this.iterator = iterator;
    }

    @Override
    public boolean hasNext() {
        return index < limit && iterator.hasNext();
    }

    @Override
    public T next() {
        if (index++ < limit){
            return iterator.next();
        }
        throw new NoSuchElementException();
    }
}
