package com.github.util.structure.iterator.page;

import com.github.util.structure.iterator.XIterator;
import java.util.*;

/**
 * 利用分页实现迭代，避免内存溢出
 * @author X1993
 * @date 2023/4/25
 * @description
 */
public class XPageIterator<T> implements XIterator<T> {

    private final XPageFunction<T> pageFunction;

    private final int pageSize;

    private XPageResult<T> pageResult;

    private int nextIndex;

    public XPageIterator(XPageFunction<T> pageFunction, int pageSize) {
        Objects.requireNonNull(pageFunction);
        this.pageFunction = pageFunction;

        if (pageSize < 1){
            throw new IllegalArgumentException();
        }
        this.pageSize = pageSize;
    }

    public XPageIterator(XPageFunction<T> pageFunction){
        this(pageFunction,200);
    }

    /**
     * 重置下一条记录读取位置
     * @param index
     */
    public void resetIndex(int index){
        getResultIndex(nextIndex = index);
    }

    @Override
    public boolean hasNext()
    {
        return getResultIndex(nextIndex) >= 0;
    }

    @Override
    public T next() {
        int resultIndex = getResultIndex(nextIndex++);
        if (resultIndex < 0){
            throw new NoSuchElementException();
        }
        return getResults().get(resultIndex);
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
            pageResult = pageFunction.select(new XPageParamImpl(index / pageSize + 1, pageSize));
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
