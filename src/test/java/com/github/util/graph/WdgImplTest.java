package com.github.util.graph;

import org.junit.Assert;
import org.junit.Test;
import java.text.MessageFormat;
import java.util.*;

public class WdgImplTest {

    final Wdg<Integer> wdg = new WdgImpl<>();

    {
        wdg.addEdge(1 ,2 ,2);
        wdg.addEdge(3 ,2 ,3);
        wdg.addEdge(3 ,1 ,2);
        wdg.addEdge(4 ,5 ,5);
        wdg.addEdge(5 ,3 ,2);
        wdg.addEdge(4 ,1 ,2);
        wdg.addEdge(0 ,4 ,1);
        wdg.addEdge(6 ,7 ,2);
    }

    @Test
    public void shortestDistance()
    {
        List<VertexDistance<Integer>> shortestDistance1 = wdg.shortestDistanceByDijkstra(4);
        testShortestDistance(shortestDistance1);

        List<VertexDistance<Integer>> shortestDistance2 = wdg.shortestDistanceByTopologicalSort(4);
        testShortestDistance(shortestDistance2);

        wdg.addEdge(4 ,0 ,12);
        List<VertexDistance<Integer>> shortestDistance3 = wdg.shortestDistanceByDijkstra(4 ,4);
        testShortestDistance(shortestDistance3);
        boolean exception = false;
        try {
            wdg.shortestDistanceByTopologicalSort(4);
        }catch (IllegalStateException e){
            exception = true;
        }
        Assert.assertTrue(exception);
        wdg.deleteEdge(4 ,0);

        wdg.addEdge(-1 ,0 ,-1);
        List<VertexDistance<Integer>> shortestDistance4 = wdg.shortestDistanceByTopologicalSort(4);
        testShortestDistance(shortestDistance4);
        exception = false;
        try {
            wdg.shortestDistanceByDijkstra(4);
        }catch (IllegalStateException e){
            exception = true;
        }
        Assert.assertTrue(exception);
        wdg.deleteEdge(-1 ,0);
    }

    private void testShortestDistance(List<VertexDistance<Integer>> shortestDistance)
    {
        for (VertexDistance<Integer> vd : shortestDistance) {
            System.out.println("key:" + vd.getVertex() + ";distance:" + vd.getDistance());
        }

        List<VertexDistance<Integer>> expected = new ArrayList<>(5);
        expected.add(new VertexDistance<>(1 ,2));
        expected.add(new VertexDistance<>(2 ,4));
        expected.add(new VertexDistance<>(5 ,5));
        expected.add(new VertexDistance<>(3 ,7));

        Assert.assertTrue(expected.size() == shortestDistance.size());

        for (int i = 0; i < shortestDistance.size(); i++) {
            VertexDistance<Integer> vd = shortestDistance.get(i);
            VertexDistance<Integer> vd2 = expected.get(i);
            Assert.assertTrue(vd.getVertex().intValue() == vd2.getVertex().intValue());
            Assert.assertTrue(vd.getDistance() == vd2.getDistance());
        }
    }

    @Test
    public void topologicalSort()
    {
        List<Integer> topologicalSort = wdg.topologicalSort();
        List<Integer> expected = Arrays.asList(6 ,7, 0, 4 ,5 ,3, 1 ,2);
        for (int i = 0; i < expected.size(); i++) {
            Assert.assertEquals(topologicalSort.get(i) ,expected.get(i));
        }

        wdg.addEdge(4 ,0 ,12);
        boolean exception = false;
        try {
            wdg.topologicalSort();
        }catch (IllegalStateException e){
            exception = true;
        }
        Assert.assertTrue(exception);
        wdg.deleteEdge(4 ,0);
    }

    @Test
    public void hasCycle()
    {
        Assert.assertFalse(wdg.hasCycle());
        wdg.addEdge(2 ,1 ,-2);
        Assert.assertTrue(wdg.hasCycle());
        wdg.deleteEdge(2 ,1);
        Assert.assertFalse(wdg.hasCycle());
        wdg.addEdge(1 ,0 ,2);
        Assert.assertTrue(wdg.hasCycle());
        wdg.deleteEdge(1 ,0);
        Assert.assertFalse(wdg.hasCycle());
    }

    @Test
    public void hasNegativeDistance()
    {
        Assert.assertFalse(wdg.hasNegativeDistance());
        wdg.addEdge(6 ,8 ,-2);
        Assert.assertTrue(wdg.hasNegativeDistance());
        wdg.deleteEdge(6 ,8);
        Assert.assertFalse(wdg.hasNegativeDistance());
    }

    @Test
    public void performanceTest()
    {
        Wdg<Integer> wdg = new WdgImpl<>();
        int count = 1000;
        for (int i = 0; i < count; i = i + 7) {
            for (int j = i + 1; j < count; j = j + 13)
            {
                wdg.addEdge(i ,j ,1);
            }
        }

        Assert.assertFalse(wdg.hasNegativeDistance());
        Assert.assertFalse(wdg.hasCycle());

        long currentTimeMillis0 = System.currentTimeMillis();
        List<Integer> topologicalSort = wdg.topologicalSort();
        System.out.println(MessageFormat.format(
                "topologicalSort time consuming {0} ms" ,
                System.currentTimeMillis() - currentTimeMillis0));

        long currentTimeMillis = System.currentTimeMillis();
        List<VertexDistance<Integer>> shortestDistanceByTopologicalSort =
                wdg.shortestDistanceByTopologicalSort(0 ,Integer.MAX_VALUE ,topologicalSort);
        System.out.println(MessageFormat.format(
                "shortestDistanceByTopologicalSort time consuming {0} ms ,total {1} ms" ,
                System.currentTimeMillis() - currentTimeMillis ,System.currentTimeMillis() - currentTimeMillis0));

        currentTimeMillis = System.currentTimeMillis();
        List<VertexDistance<Integer>> shortestDistanceByDijkstra = wdg.shortestDistanceByDijkstra(0);
        System.out.println(MessageFormat.format(
                "shortestDistanceByDijkstra time consuming {0} ms" ,
                System.currentTimeMillis() - currentTimeMillis));

        Comparator<VertexDistance<Integer>> comparator = (vd1 ,vd2) -> {
            float v = vd1.getDistance() - vd2.getDistance();
            if (v == 0){
                return vd1.getVertex().compareTo(vd2.getVertex());
            }
            return v > 0 ? 1 : -1;
        };

        shortestDistanceByDijkstra.sort(comparator);
        shortestDistanceByDijkstra.sort(comparator);

        for (int i = 0; i < shortestDistanceByDijkstra.size(); i++) {
            VertexDistance<Integer> vd1 = shortestDistanceByDijkstra.get(i);
            VertexDistance<Integer> vd2 = shortestDistanceByTopologicalSort.get(i);
            Assert.assertTrue(vd1.getDistance()  == vd2.getDistance());
            Assert.assertEquals(vd1.getVertex() ,vd2.getVertex());
        }
    }

}