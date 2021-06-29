package com.github.util;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 加权有向图实现
 * @Author: junjie
 * @Date: 2019/7/24
 */
public class WDGImpl<V ,L> implements WDG<V ,L> {

    /**
     * 点-相邻点 集
     */
    private final Map<V ,Set<DirectedEdge<V ,L>>> vertexMap = new HashMap<>();

    @Override
    public boolean addVertex(V vertex) {
        return vertexMap.computeIfAbsent(vertex ,s -> new HashSet<>()) != null;
    }

    @Override
    public boolean addEdge(V source, V target, Distance<L> distance)
    {
        addVertex(target);
        return vertexMap.computeIfAbsent(source, s -> new HashSet<>()).add(new DirectedEdge<>(source, target, distance));
    }

    @Override
    public boolean deleteEdge(V source, V target)
    {
        Set<DirectedEdge<V, L>> edges = vertexMap.get(source);
        if (edges != null && edges.size() > 0){
            return edges.remove(new DirectedEdge<>(source, target, null));
        }
        return false;
    }

    @Override
    public boolean hasCycle()
    {
        return hasCycle(v -> {});
    }

    @Override
    public boolean hasNegativeDistance()
    {
        return vertexMap.values()
                .stream()
                .anyMatch(set -> set.stream()
                        .anyMatch(edge -> edge.getDistance().isNegative()));
    }

    private boolean hasCycle(Consumer<V> stackPopConsumer)
    {
        Set<V> unTraversedVertexSet = new HashSet<>(vertexMap.keySet());

        V currentVertex = null;
        //dfs
        while ((currentVertex = unTraversedVertexSet.stream().findAny().orElse(null)) != null)
        {
            Stack<List<V>> methodStack = new Stack<>();

            List<V> startEntry = new ArrayList<>();
            startEntry.add(currentVertex);
            methodStack.add(startEntry);

            //模拟方法调用链，检测环
            Set<V> traversingVertexSet = new HashSet<>();
            Stack<V> traversingVertexStack = new Stack<>();

            while (!methodStack.isEmpty())
            {
                List<V> topEntry = methodStack.peek();
                if (topEntry.isEmpty()){
                    //模拟方法返回
                    methodStack.pop();
                    if (traversingVertexStack.isEmpty()){
                        continue;
                    }
                    V popVertex = traversingVertexStack.pop();
                    if (popVertex != null){
                        stackPopConsumer.accept(popVertex);
                        traversingVertexSet.remove(popVertex);
                    }
                }else {
                    V popVertex = topEntry.remove(0);
                    if (traversingVertexSet.contains(popVertex)){
                        return true;
                    }
                    //剪枝
                    if (unTraversedVertexSet.remove(popVertex))
                    {
                        List<V> nextEntry = vertexMap.getOrDefault(popVertex,
                                (Set<DirectedEdge<V, L>>) Collections.EMPTY_SET).stream()
                                .map(DirectedEdge::getTarget)
                                .collect(Collectors.toList());

                        //模拟方法调用
                        methodStack.add(nextEntry);
                        traversingVertexSet.add(popVertex);
                        traversingVertexStack.push(popVertex);
                    }
                }
            }
        }

        return false;
    }

    @Override
    public List<V> topologicalSort()
    {
        Stack<V> sortStack = new Stack<>();
        if (hasCycle(v -> sortStack.push(v))){
            throw new IllegalStateException("has cycle");
        }

        List<V> topologicalSort = new ArrayList<>(sortStack.size());
        V currentVertex;
        while (!sortStack.isEmpty() && (currentVertex = sortStack.pop()) != null){
            topologicalSort.add(currentVertex);
        }

        return topologicalSort;
    }

    @Override
    public List<VertexDistance<V, L>> shortestDistance(V source, int num)
    {
        if (!hasNegativeDistance()){
            return shortestDistanceByDijkstra(source ,num);
        }
        try {
            return shortestDistanceByTopologicalSort(source ,num);
        }catch (IllegalStateException e){

        }
        //TODO
        throw new IllegalStateException();
    }

    @Override
    public List<VertexDistance<V ,L>> shortestDistanceByTopologicalSort(V source, int num ,List<V> topologicalSort)
    {
        Map<V ,Distance<L>> vertexMinDistanceMap = new HashMap<>(vertexMap.size());
        boolean match = false;

        int size = topologicalSort.size();
        for (int i = 0; i < size; i++)
        {
            V vertex = topologicalSort.get(i);
            if (match){
                Set<DirectedEdge<V, L>> directedEdges = vertexMap.get(vertex);
                if (directedEdges != null && directedEdges.size() > 0){
                    for (DirectedEdge<V, L> directedEdge : directedEdges)
                    {
                        V edgeSource = directedEdge.getSource();
                        V edgeTarget = directedEdge.getTarget();
                        Distance<L> edgeDistance = directedEdge.getDistance();

                        if (edgeSource.equals(source)){
                            vertexMinDistanceMap.put(edgeTarget ,edgeDistance);
                        }else {
                            Distance<L> edgeTargetOldMinDistance = vertexMinDistanceMap.get(edgeTarget);
                            Distance<L> edgeSourceOldMinDistance = vertexMinDistanceMap.get(edgeSource);

                            Distance<L> edgeTargetNewMinDistance = edgeTargetOldMinDistance;
                            if (edgeSourceOldMinDistance != null){
                                edgeTargetNewMinDistance = edgeSourceOldMinDistance.add(edgeDistance);

                                if (edgeTargetOldMinDistance != null) {
                                    edgeTargetNewMinDistance = edgeTargetNewMinDistance.compareTo(edgeTargetOldMinDistance)
                                            < 0 ? edgeTargetNewMinDistance : edgeTargetOldMinDistance;
                                }
                            }

                            vertexMinDistanceMap.put(edgeTarget ,edgeTargetNewMinDistance);
                        }
                    }
                }
            } else if (vertex.equals(source))
            {
                match = true;
                i--;
            }
        }

        List<VertexDistance<V ,L>> vertexDistances = new ArrayList<>(vertexMinDistanceMap.size());
        vertexDistances.remove(source);
        for (Map.Entry<V, Distance<L>> entry : vertexMinDistanceMap.entrySet()) {
            vertexDistances.add(new VertexDistance(entry.getKey() ,entry.getValue()));
        }

        vertexDistances.sort((vt1 ,vt2) -> vt1.getDistance().compareTo(vt2.getDistance()));
        return vertexDistances;
    }

    @Override
    public List<VertexDistance<V ,L>> shortestDistanceByDijkstra(V source, int num)
    {
        if (hasNegativeDistance()){
            throw new IllegalStateException("has negative distance");
        }

        PriorityQueue<VertexDistance<V ,L>> minimumHeap = new PriorityQueue<>();
        List<VertexDistance<V, L>> nearestVertex = new LinkedList<>();
        Set<V> traversal = new HashSet<>();

        for (DirectedEdge<V, L> edge : vertexMap.get(source)) {
            minimumHeap.add(new VertexDistance(edge.getTarget() ,edge.getDistance()));
        }
        traversal.add(source);

        VertexDistance<V, L> nearest = null;
        while ((nearest = minimumHeap.poll()) != null){
            V vertex = nearest.getVertex();
            if (traversal.contains(vertex)) {
                continue;
            }

            nearestVertex.add(nearest);
            if (nearestVertex.size() >= num) {
                break;
            }
            traversal.add(vertex);

            Distance<L> shortestDistance = nearest.getDistance();
            Set<DirectedEdge<V, L>> directedEdges = vertexMap.get(vertex);
            if (directedEdges != null && directedEdges.size() > 0) {
                for (DirectedEdge<V, L> edge : vertexMap.get(vertex)) {
                    if (traversal.contains(edge.getTarget())) {
                        continue;
                    }
                    minimumHeap.add(new VertexDistance<>(edge.getTarget(), shortestDistance.add(edge.getDistance())));
                }
            }
        }

        return nearestVertex;
    }


}
