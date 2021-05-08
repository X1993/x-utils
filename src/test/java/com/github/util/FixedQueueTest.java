package com.github.util;

import org.junit.Assert;
import org.junit.Test;

public class FixedQueueTest {

    @Test
    public void test()
    {
        FixedQueue<Integer> fixedQueue =  new FixedQueue<>(3);
        Assert.assertEquals(fixedQueue.size() ,0);
        Assert.assertTrue(fixedQueue.isEmpty());

        int value = 0;

        fixedQueue.push(value++);
        fixedQueue.push(value++);
        Assert.assertEquals(fixedQueue.size() ,2);
        Assert.assertFalse(fixedQueue.isEmpty());

        Assert.assertEquals(fixedQueue.get(0).intValue() ,0);
        Assert.assertEquals(fixedQueue.get(1).intValue() ,1);
        Assert.assertNull(fixedQueue.get(2));

        fixedQueue.push(value++);
        fixedQueue.push(value++);
        fixedQueue.push(value++);
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

        fixedQueue.push(value++);
        fixedQueue.push(value++);
        Assert.assertEquals(fixedQueue.size() ,3);

        v = 4;
        for (Integer integer : fixedQueue.list()) {
            Assert.assertTrue(integer.intValue() == v++);
        }
    }

}