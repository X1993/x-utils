package com.github.util.graph;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 距离某个点的距离
 * @Author: junjie
 * @Date: 2021/8/9
 * @param <T>
 */
@AllArgsConstructor
@Data
public class VertexDistance<T> implements Comparable<VertexDistance<T>>{

    /**
     * 相邻点
     */
    final T vertex;

    /**
     * 距离
     */
    final float distance;

    @Override
    public int compareTo(VertexDistance<T> o) {
        float v = getDistance() - o.getDistance();
        return v > 0 ? 1 : (v == 0 ? 0 : -1);
    }

}
