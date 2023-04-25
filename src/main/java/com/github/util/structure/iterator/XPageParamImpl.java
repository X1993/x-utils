package com.github.util.structure.iterator;

/**
 * @author X1993
 * @date 2023/4/25
 * @description
 */
public final class XPageParamImpl implements XPageParam{

    private final int pageIndex;

    private final int pageSize;

    public XPageParamImpl(int pageIndex, int pageSize) {
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
    }

    @Override
    public int pageIndex() {
        return pageIndex;
    }

    @Override
    public int pageSize() {
        return pageSize;
    }

}
