package com.github.util.structure;

import org.junit.Assert;
import org.junit.Test;
import java.util.Map;

/**
 * @author wangjj7
 * @date 2022/12/30
 * @description
 */
public class WeakValueHashMapTest {

    @Test
    public void getLockTest() throws InterruptedException {
        Map<String ,Object> recyclableLock = new WeakValueHashMap<>();

        String key1 = "001";
        Object lock0 = recyclableLock.computeIfAbsent(key1 ,x -> new Object());
        String lockAddress0 = lock0.toString();

        System.gc();
        Thread.sleep(1000);//wait gc

        Object lock1 = recyclableLock.computeIfAbsent(key1 ,x -> new Object());
        Assert.assertTrue(lock0 == lock1);

        lock0 = null;
        lock1 = null;
        System.gc();
        Thread.sleep(1000);//wait gc

        Object lock2 = recyclableLock.computeIfAbsent(key1 ,x -> new Object());
        String lockAddress2 = lock2.toString();
        Assert.assertNotEquals(lockAddress0 ,lockAddress2);
    }

}