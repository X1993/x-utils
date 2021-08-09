package com.github.util.graph;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.Objects;

/**
 * 有向边
 * @Author: jie
 * @Date: 2019/7/24
 */
@AllArgsConstructor
@Data
public final class DirectedEdge<V> implements Comparable<DirectedEdge<V>>{

    /**
     * 起始点
     */
    final private V source;

    /**
     * 目标点
     */
    final private V target;

    /**
     * 距离
     */
    final private float distance;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DirectedEdge<?> that = (DirectedEdge<?>) o;
        return Objects.equals(source, that.source) &&
                Objects.equals(target, that.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, target);
    }

    @Override
    public int compareTo(DirectedEdge<V> edge) {
        float v = getDistance() - edge.getDistance();
        return v > 0 ? 1 : (v == 0 ? 0 : -1);
    }
}
