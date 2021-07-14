package com.github.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;
import java.util.Set;

/**
 * 加权有向图
 * @Author: junjie
 * @Date: 2019/7/24
 */
public interface WDG<V ,L> {

    /**
     * 添加顶点
     * @param vertex
     * @return
     */
    boolean addVertex(V vertex);

    /**
     * 添加一条有向边，如果顶点不存在，默认添加
     * @param source
     * @param target
     * @param distance
     */
    boolean addEdge(V source, V target, Distance<L> distance);

    /**
     * 添加两条双向的有向边
     * @param v1
     * @param v2
     * @param distance
     */
    default void addBilateralEdge(V v1, V v2, Distance<L> distance){
        addEdge(v1 ,v2 ,distance);
        addEdge(v2, v1, distance);
    }

    /**
     * 删除一条边
     * @param source
     * @param target
     */
    boolean deleteEdge(V source , V target);

    Set<DirectedEdge<V ,L>> getDirectedEdge(V vertex);

    /**
     * 是否存在环
     * @return
     */
    boolean hasCycle();

    /**
     * 是否存在负权重边
     * @return
     */
    boolean hasNegativeDistance();

    /**
     * 拓扑排序
     * @return
     */
    List<V> topologicalSort();

    /**
     * 按从近到远的顺序返回指定点的相邻点
     * @param source 指定的点
     * @param num 只返回最近num个相邻点
     * @return
     */
    List<VertexDistance<V ,L>> shortestDistance(V source, int num);

    default List<VertexDistance<V ,L>> shortestDistance(V source){
        return shortestDistance(source ,Integer.MAX_VALUE);
    }

    /**
     * 通过拓扑排序实现从近到远的顺序返回指定点的相邻点
     * @param source 指定的点
     * @param num 只返回最近num个相邻点
     * @param topologicalSort 拓扑排序结果集
     * @return
     */
    List<VertexDistance<V, L>> shortestDistanceByTopologicalSort(V source, int num, List<V> topologicalSort);

    default List<VertexDistance<V, L>> shortestDistanceByTopologicalSort(V source, int num){
        return shortestDistanceByTopologicalSort(source ,num ,topologicalSort());
    }

    default List<VertexDistance<V ,L>> shortestDistanceByTopologicalSort(V source){
        return shortestDistanceByTopologicalSort(source ,Integer.MAX_VALUE);
    }

    /**
     * 通过Dijkstra实现从近到远的顺序返回指定点的相邻点
     * @param source 指定的点
     * @param num 只返回最近num个相邻点
     * @return
     */
    List<VertexDistance<V, L>> shortestDistanceByDijkstra(V source, int num);

    default List<VertexDistance<V ,L>> shortestDistanceByDijkstra(V source){
        return shortestDistanceByDijkstra(source ,Integer.MAX_VALUE);
    }

    /**
     * 保存相邻点及距离
     * @param <T>
     * @param <L>
     */
    @AllArgsConstructor
    @Data
    class VertexDistance<T ,L> implements Comparable<VertexDistance<T ,L>>{

        /**
         * 相邻点
         */
        final T vertex;

        /**
         * 距离
         */
        final Distance<L> distance;

        @Override
        public int compareTo(VertexDistance<T, L> o) {
            return distance.compareTo(o.distance);
        }
    }

}
