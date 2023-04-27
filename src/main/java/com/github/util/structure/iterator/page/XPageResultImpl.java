package com.github.util.structure.iterator.page;

import java.util.List;
import java.util.Objects;

/**
 * @author X1993
 * @date 2023/4/25
 * @description
 */
public class XPageResultImpl<T> implements XPageResult<T>{

    private final int pageIndex;

    private final int pageSize;

    private final List<T> results;

    public XPageResultImpl(int pageIndex, int pageSize, List<T> results) {
        Objects.requireNonNull(results);
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
        this.results = results;
    }

    public XPageResultImpl(XPageParam pageParam ,List<T> results){
        this(pageParam.pageIndex(),  pageParam.pageSize() ,results);
    }

    @Override
    public int pageIndex() {
        return pageIndex;
    }

    @Override
    public int pageSize() {
        return pageSize;
    }

    @Override
    public List<T> results() {
        return results;
    }

}
