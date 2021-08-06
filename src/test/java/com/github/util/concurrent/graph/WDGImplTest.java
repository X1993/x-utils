package com.github.util.concurrent.graph;

import com.github.util.graph.FloatDistance;
import com.github.util.graph.WDG;
import com.github.util.graph.WDGImpl;
import org.junit.Assert;
import org.junit.Test;
import java.text.MessageFormat;
import java.util.*;

public class WDGImplTest {

    final WDG<Integer ,Float> wdg = new WDGImpl<>();

    {
        wdg.addEdge(1 ,2 ,new FloatDistance(2));
        wdg.addEdge(3 ,2 ,new FloatDistance(3));
        wdg.addEdge(3 ,1 ,new FloatDistance(2));
        wdg.addEdge(4 ,5 ,new FloatDistance(5));
        wdg.addEdge(5 ,3 ,new FloatDistance(2));
        wdg.addEdge(4 ,1 ,new FloatDistance(2));
        wdg.addEdge(0 ,4 ,new FloatDistance(1));
        wdg.addEdge(6 ,7 ,new FloatDistance(2));
    }

    @Test
    public void shortestDistance()
    {
        List<WDG.VertexDistance<Integer, Float>> shortestDistance1 = wdg.shortestDistanceByDijkstra(4);
        testShortestDistance(shortestDistance1);

        List<WDG.VertexDistance<Integer, Float>> shortestDistance2 = wdg.shortestDistanceByTopologicalSort(4);
        testShortestDistance(shortestDistance2);

        wdg.addEdge(4 ,0 ,new FloatDistance(12));
        List<WDG.VertexDistance<Integer, Float>> shortestDistance3 = wdg.shortestDistanceByDijkstra(4 ,4);
        testShortestDistance(shortestDistance3);
        boolean exception = false;
        try {
            wdg.shortestDistanceByTopologicalSort(4);
        }catch (IllegalStateException e){
            exception = true;
        }
        Assert.assertTrue(exception);
        wdg.deleteEdge(4 ,0);

        wdg.addEdge(-1 ,0 ,new FloatDistance(-1));
        List<WDG.VertexDistance<Integer, Float>> shortestDistance4 = wdg.shortestDistanceByTopologicalSort(4);
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

    private void testShortestDistance(List<WDG.VertexDistance<Integer, Float>> shortestDistance)
    {
        for (WDG.VertexDistance<Integer, Float> vd : shortestDistance) {
            System.out.println("key:" + vd.getVertex() + ";distance:" + vd.getDistance().get());
        }

        List<WDG.VertexDistance<Integer, Float>> expected = new ArrayList<>(5);
        expected.add(new WDG.VertexDistance<>(1 ,new FloatDistance(2)));
        expected.add(new WDG.VertexDistance<>(2 ,new FloatDistance(4)));
        expected.add(new WDG.VertexDistance<>(5 ,new FloatDistance(5)));
        expected.add(new WDG.VertexDistance<>(3 ,new FloatDistance(7)));

        Assert.assertTrue(expected.size() == shortestDistance.size());

        for (int i = 0; i < shortestDistance.size(); i++) {
            WDG.VertexDistance<Integer, Float> vd = shortestDistance.get(i);
            WDG.VertexDistance<Integer, Float> vd2 = expected.get(i);
            Assert.assertTrue(vd.getVertex().intValue() == vd2.getVertex().intValue());
            Assert.assertTrue(vd.getDistance().get().floatValue() == vd2.getDistance().get().floatValue());
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

        wdg.addEdge(4 ,0 ,new FloatDistance(12));
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
        wdg.addEdge(2 ,1 ,new FloatDistance(-2));
        Assert.assertTrue(wdg.hasCycle());
        wdg.deleteEdge(2 ,1);
        Assert.assertFalse(wdg.hasCycle());
        wdg.addEdge(1 ,0 ,new FloatDistance(2));
        Assert.assertTrue(wdg.hasCycle());
        wdg.deleteEdge(1 ,0);
        Assert.assertFalse(wdg.hasCycle());
    }

    @Test
    public void hasNegativeDistance()
    {
        Assert.assertFalse(wdg.hasNegativeDistance());
        wdg.addEdge(6 ,8 ,new FloatDistance(-2));
        Assert.assertTrue(wdg.hasNegativeDistance());
        wdg.deleteEdge(6 ,8);
        Assert.assertFalse(wdg.hasNegativeDistance());
    }

    @Test
    public void performanceTest()
    {
        WDG<Integer ,Float> wdg = new WDGImpl<>();
        int count = 1000;
        for (int i = 0; i < count; i = i + 7) {
            for (int j = i + 1; j < count; j = j + 13)
            {
                wdg.addEdge(i ,j ,new FloatDistance(1));
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
        List<WDG.VertexDistance<Integer, Float>> shortestDistanceByTopologicalSort =
                wdg.shortestDistanceByTopologicalSort(0 ,Integer.MAX_VALUE ,topologicalSort);
        System.out.println(MessageFormat.format(
                "shortestDistanceByTopologicalSort time consuming {0} ms ,total {1} ms" ,
                System.currentTimeMillis() - currentTimeMillis ,System.currentTimeMillis() - currentTimeMillis0));

        currentTimeMillis = System.currentTimeMillis();
        List<WDG.VertexDistance<Integer, Float>> shortestDistanceByDijkstra = wdg.shortestDistanceByDijkstra(0);
        System.out.println(MessageFormat.format(
                "shortestDistanceByDijkstra time consuming {0} ms" ,
                System.currentTimeMillis() - currentTimeMillis));

        Comparator<WDG.VertexDistance<Integer, Float>> comparator = (vd1 ,vd2) -> {
            int i = vd1.getDistance().get().compareTo(vd2.getDistance().get());
            if (i == 0){
                return vd1.getVertex().compareTo(vd2.getVertex());
            }
            return i;
        };

        shortestDistanceByDijkstra.sort(comparator);
        shortestDistanceByDijkstra.sort(comparator);

        for (int i = 0; i < shortestDistanceByDijkstra.size(); i++) {
            WDG.VertexDistance<Integer, Float> vd1 = shortestDistanceByDijkstra.get(i);
            WDG.VertexDistance<Integer, Float> vd2 = shortestDistanceByTopologicalSort.get(i);
            Assert.assertEquals(vd1.getDistance() ,vd2.getDistance());
            Assert.assertEquals(vd1.getVertex() ,vd2.getVertex());
        }
    }

}