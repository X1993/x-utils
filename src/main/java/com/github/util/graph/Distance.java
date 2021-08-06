package com.github.util.graph;

/**
 * 距离
 * @Author: jie
 * @Date: 2019/7/24
 */
public interface Distance<T> extends Comparable<Distance<T>> {

    /**
     * 累加
     * @param distance
     * @return
     */
    Distance<T> add(Distance<T> distance);

    /**
     * 获取值
     * @return
     */
    T get();

    /**
     * 是否负值
     * @return
     */
    boolean isNegative();

}
