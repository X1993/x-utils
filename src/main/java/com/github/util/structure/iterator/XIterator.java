package com.github.util.structure.iterator;

import com.github.util.structure.iterator.limit.XLimitIterator;
import java.util.Iterator;

/**
 * @author X1993
 * @date 2023/4/27
 * @description
 */
public interface XIterator<T> extends Iterator<T> {

    /**
     * 限制最大迭代次数
     * @param limit 最大迭代次数
     * @return
     */
    default XIterator<T> limit(int limit){
        return new XLimitIterator(this ,limit);
    }

}
