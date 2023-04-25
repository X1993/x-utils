package com.github.util.structure.iterator;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * 利用分页实现迭代，避免内存溢出
 * @author X1993
 * @date 2023/4/25
 * @description
 */
public class XPageIterator<T> implements Iterator<T>{

    private final XPageFunction<T> xPageFunction;

    private final int pageSize;

    private XPageResult<T> pageResult;

    private int nextIndex = 0;

    public XPageIterator(XPageFunction<T> xPageFunction, int pageSize) {
        Objects.requireNonNull(xPageFunction);
        this.xPageFunction = xPageFunction;

        if (pageSize < 1){
            throw new IllegalArgumentException();
        }
        this.pageSize = pageSize;
    }

    public XPageIterator(XPageFunction<T> xPageFunction){
        this(xPageFunction,200);
    }

    @Override
    public boolean hasNext()
    {
        return getResultIndex(nextIndex) >= 0;
    }

    @Override
    public T next() {
        int resultIndex = getResultIndex(nextIndex++);
        return resultIndex < 0 ? null : getResults().get(resultIndex);
    }

    private int getResultIndex(int index) {
        while (true) {
            if (pageResult != null) {
                List<T> partitionResults = getResults();
                int minIndex = (pageResult.pageIndex() - 1) * pageResult.pageSize();
                int maxIndex = minIndex + partitionResults.size() - 1;
                if (minIndex <= index && index <= maxIndex) {
                    //当前分页结果集中有目标数据
                    return index % pageSize;
                }
                if (index > maxIndex && partitionResults.size() < pageSize){
                    //没有更多数据了
                    return -1;
                }
            }
            //根据index定位到指定的分页结果集
            pageResult = xPageFunction.select(new XPageParamImpl(index / pageSize + 1, pageSize));
            //check result
            if (pageResult == null){
                throw new IllegalArgumentException("pageFunction查询结果不能是null");
            }
            List<T> partitionResults = getResults();
            if (partitionResults.size() > pageSize){
                throw new IllegalStateException("pageFunction查询结果的数量不能大于pageSize");
            }
        }
    }

    private List<T> getResults(){
        List<T> partitionResults = pageResult.results();
        return partitionResults == null ? Collections.EMPTY_LIST : partitionResults;
    }

}
