package com.github.util.structure.iterator.page;

import com.github.util.structure.iterator.XIterable;
import com.github.util.structure.iterator.XIterator;

/**
 * 利用分页实现迭代，避免内存溢出
 * @author X1993
 * @date 2023/4/25
 * @description
 */
public class XPageIterable<T> implements XIterable<T> {

    private final XPageFunction<T> pageFunction;

    private final Integer pageSize;

    public XPageIterable(XPageFunction<T> pageFunction, int pageSize) {
        this.pageFunction = pageFunction;
        if (pageSize < 1){
            throw new IllegalArgumentException();
        }
        this.pageSize = pageSize;
    }

    public XPageIterable(XPageFunction<T> pageFunction){
        this.pageFunction = pageFunction;
        this.pageSize = null;
    }

    @Override
    public XIterator<T> iterator() {
        return pageSize != null ? new XPageIterator<>(pageFunction) : new XPageIterator<>(pageFunction ,pageSize);
    }

}
