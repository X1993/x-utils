package com.github.util;

import org.junit.Assert;
import org.junit.Test;
import java.util.Arrays;

public class FixedQueueTest {

    @Test
    public void test()
    {
        FixedQueue<Integer> fixedQueue =  new FixedQueue<>(3);
        Assert.assertEquals(fixedQueue.size() ,0);
        Assert.assertTrue(fixedQueue.isEmpty());

        int value = 0;

        fixedQueue.add(value++);
        fixedQueue.add(value++);
        Assert.assertEquals(fixedQueue.size() ,2);
        Assert.assertFalse(fixedQueue.isEmpty());

        Assert.assertEquals(fixedQueue.get(0).intValue() ,0);
        Assert.assertEquals(fixedQueue.getFirst().intValue() ,0);
        Assert.assertEquals(fixedQueue.get(1).intValue() ,1);
        Assert.assertNull(fixedQueue.get(2));

        fixedQueue.add(value++);
        fixedQueue.add(value++);
        fixedQueue.offer(value++);
        Assert.assertEquals(fixedQueue.size() ,3);
        Assert.assertEquals(fixedQueue.get(0).intValue() ,2);
        Assert.assertEquals(fixedQueue.get(1).intValue() ,3);

        int v = 2;
        for (Integer integer : fixedQueue.list()) {
            Assert.assertTrue(integer.intValue() == v++);
        }

        Assert.assertEquals(fixedQueue.poll().intValue() ,2);
        Assert.assertEquals(fixedQueue.poll().intValue() ,3);
        Assert.assertEquals(fixedQueue.size() ,1);
        Assert.assertEquals(fixedQueue.get(0).intValue() ,4);
        Assert.assertNull(fixedQueue.get(1));

        fixedQueue.offer(value++);
        fixedQueue.add(value++);
        Assert.assertEquals(fixedQueue.size() ,3);

        v = 4;
        for (Integer integer : fixedQueue.list()) {
            Assert.assertTrue(integer.intValue() == v++);
        }

        fixedQueue.addAll(Arrays.asList(10 ,11 ,12));
        Assert.assertEquals(fixedQueue.size() ,3);

        Assert.assertTrue(fixedQueue.remove(11));
        Assert.assertEquals(fixedQueue.size() ,2);
        Assert.assertTrue(fixedQueue.getFirst().intValue() == 10);
        Assert.assertTrue(fixedQueue.get(0).intValue() == 10);
        Assert.assertTrue(fixedQueue.getLast().intValue() == 12);
        Assert.assertTrue(fixedQueue.get(1).intValue() == 12);
        Assert.assertNull(fixedQueue.get(2));

        fixedQueue.addAll(Arrays.asList(12 ,11 ,12));

        Assert.assertTrue(fixedQueue.remove(12));
        Assert.assertEquals(fixedQueue.size() ,1);
        Assert.assertTrue(fixedQueue.getFirst().intValue() == 11);
        Assert.assertTrue(fixedQueue.get(0).intValue() == 11);
        Assert.assertTrue(fixedQueue.getLast().intValue() == 11);
        Assert.assertNull(fixedQueue.get(1));


        fixedQueue.addAll(Arrays.asList(12 ,11 ,12));

        Assert.assertTrue(fixedQueue.removeAll(Arrays.asList(11 ,12)));
        Assert.assertEquals(fixedQueue.size() ,0);
        Assert.assertNull(fixedQueue.getFirst());
        Assert.assertNull(fixedQueue.get(0));


    }

}