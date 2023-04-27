package com.github.util.structure.iterator;

import com.github.util.structure.iterator.limit.XLimitIterable;

/**
 * @author X1993
 * @date 2023/4/27
 * @description
 */
public interface XIterable<T> extends Iterable<T>{

    default XIterable limit(int limit){
        return new XLimitIterable(this ,limit);
    }

}
