package com.github.util.structure.iterator.page;

import java.util.List;

/**
 * 分页查询结果
 * @author X1993
 * @date 2023/4/25
 * @description
 */
public interface XPageResult<T> extends XPageParam {

    /**
     * 分页查询结果
     * @return
     */
    List<T> results();

}
