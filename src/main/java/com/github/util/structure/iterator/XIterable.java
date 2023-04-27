package com.github.util.structure.iterator;

import com.github.util.structure.iterator.limit.XLimitIterable;

/**
 * @author X1993
 * @date 2023/4/27
 * @description
 */
public interface XIterable<T> extends Iterable<T>{

    /**
     * 限制最大迭代次数
     * @param limit 最大迭代次数
     * @return
     */
    default XIterable<T> limit(int limit){
        return new XLimitIterable(this ,limit);
    }

}
