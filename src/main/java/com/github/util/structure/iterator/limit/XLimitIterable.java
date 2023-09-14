package com.github.util.structure.iterator.limit;

import com.github.util.structure.iterator.XIterable;
import java.util.Iterator;

/**
 * 限制最大迭代次数的迭代器
 * @author X1993
 * @date 2023/4/27
 * @description
 */
public class XLimitIterable<T> implements XIterable<T> {

    private final Iterable<T> iterable;

    private final int limit;

    public XLimitIterable(Iterable<T> iterable ,int limit){
        if (iterable == null){
            throw new NullPointerException();
        }
        this.iterable = iterable;
        this.limit = limit;
    }

    @Override
    public Iterator<T> iterator() {
        return new XLimitIterator<>(iterable.iterator() ,limit);
    }

}
