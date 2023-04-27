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

    private XLimitIterator<T> iterator;

    public XLimitIterable(Iterable<T> iterable ,int limit){
        this.iterator = new XLimitIterator<>(iterable.iterator() ,limit);
    }

    public XLimitIterable(Iterator<T> iterator ,int limit){
        this.iterator = new XLimitIterator<>(iterator ,limit);
    }

    @Override
    public Iterator<T> iterator() {
        return iterator;
    }

}
