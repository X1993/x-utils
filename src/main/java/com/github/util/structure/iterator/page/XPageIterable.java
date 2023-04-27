package com.github.util.structure.iterator.page;

import java.util.Iterator;

/**
 * 利用分页实现迭代，避免内存溢出
 * @author X1993
 * @date 2023/4/25
 * @description
 */
public class XPageIterable<T> implements Iterable<T>{

    private XPageIterator<T> iterator;

    public XPageIterable(XPageFunction<T> pageFunction, int pageSize) {
        this.iterator = new XPageIterator<>(pageFunction, pageSize);
    }

    public XPageIterable(XPageFunction<T> pageFunction){
        this.iterator = new XPageIterator<>(pageFunction);
    }

    @Override
    public Iterator<T> iterator() {
        return iterator;
    }

}
