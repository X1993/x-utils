package com.github.util.graph;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 加权有向图实现
 * @Author: jie
 * @Date: 2019/7/24
 */
public class WdgImpl<V> implements Wdg<V> {

    /**
     * 点-相邻点 集
     */
    private final Map<V ,Set<DirectedEdge<V>>> vertexMap = new HashMap<>();

    @Override
    public boolean addVertex(V vertex) {
        return vertexMap.computeIfAbsent(vertex ,s -> new HashSet<>()) != null;
    }

    @Override
    public boolean addEdge(V source, V target, float distance)
    {
        addVertex(target);
        return vertexMap.computeIfAbsent(source, s -> new HashSet<>()).add(new DirectedEdge<>(source, target, distance));
    }

    @Override
    public boolean deleteEdge(V source, V target)
    {
        return getDirectedEdge(source).remove(new DirectedEdge<>(source, target, 0));
    }

    @Override
    public Set<DirectedEdge<V>> getDirectedEdge(V vertex){
        return vertexMap.getOrDefault(vertex ,Collections.EMPTY_SET);
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
                        .anyMatch(edge -> edge.getDistance() < 0));
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
                        List<V> nextEntry = getDirectedEdge(popVertex).stream()
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
    public List<VertexDistance<V>> shortestDistance(V source, int num)
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
    public List<VertexDistance<V>> shortestDistanceByTopologicalSort(V source, int num ,List<V> topologicalSort)
    {
        Map<V ,Float> vertexMinDistanceMap = new HashMap<>(vertexMap.size());
        boolean match = false;

        int size = topologicalSort.size();
        for (int i = 0; i < size; i++)
        {
            V vertex = topologicalSort.get(i);
            if (match){
                Set<DirectedEdge<V>> directedEdges = getDirectedEdge(vertex);
                for (DirectedEdge<V> directedEdge : directedEdges)
                {
                    V edgeSource = directedEdge.getSource();
                    V edgeTarget = directedEdge.getTarget();
                    float edgeDistance = directedEdge.getDistance();

                    if (edgeSource.equals(source)){
                        vertexMinDistanceMap.put(edgeTarget ,edgeDistance);
                    }else {
                        Float edgeTargetOldMinDistance = vertexMinDistanceMap.get(edgeTarget);
                        Float edgeSourceOldMinDistance = vertexMinDistanceMap.get(edgeSource);

                        Float edgeTargetNewMinDistance = edgeTargetOldMinDistance;
                        if (edgeSourceOldMinDistance != null){
                            edgeTargetNewMinDistance = edgeSourceOldMinDistance + edgeDistance;

                            if (edgeTargetOldMinDistance != null) {
                                edgeTargetNewMinDistance = edgeTargetNewMinDistance.compareTo(edgeTargetOldMinDistance)
                                        < 0 ? edgeTargetNewMinDistance : edgeTargetOldMinDistance;
                            }
                        }

                        vertexMinDistanceMap.put(edgeTarget ,edgeTargetNewMinDistance);
                    }
                }
            } else if (vertex.equals(source))
            {
                match = true;
                i--;
            }
        }

        List<VertexDistance<V>> vertexDistances = new ArrayList<>(vertexMinDistanceMap.size());
        vertexDistances.remove(source);
        for (Map.Entry<V, Float> entry : vertexMinDistanceMap.entrySet()) {
            vertexDistances.add(new VertexDistance(entry.getKey() ,entry.getValue()));
        }

        vertexDistances.sort((vt1 ,vt2) -> vt1.compareTo(vt2));
        return vertexDistances;
    }

    @Override
    public List<VertexDistance<V>> shortestDistanceByDijkstra(V source, int num)
    {
        if (hasNegativeDistance()){
            throw new IllegalStateException("has negative distance");
        }

        PriorityQueue<VertexDistance<V>> minimumHeap = new PriorityQueue<>();
        List<VertexDistance<V>> nearestVertex = new LinkedList<>();
        Set<V> traversal = new HashSet<>();

        for (DirectedEdge<V> edge : getDirectedEdge(source)) {
            minimumHeap.add(new VertexDistance(edge.getTarget() ,edge.getDistance()));
        }
        traversal.add(source);

        VertexDistance<V> nearest = null;
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

            float shortestDistance = nearest.getDistance();
            for (DirectedEdge<V> edge : getDirectedEdge(vertex)) {
                if (traversal.contains(edge.getTarget())) {
                    continue;
                }
                minimumHeap.add(new VertexDistance<>(edge.getTarget(), shortestDistance + edge.getDistance()));
            }
        }

        return nearestVertex;
    }


}
