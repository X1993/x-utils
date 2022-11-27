package com.github.util.concurrent;

import org.junit.Assert;
import org.junit.Test;

import java.util.function.Supplier;

/**
 * @author wangjj7
 * @date 2022/11/25
 * @description
 */
public class RecyclableLockTest {

    @Test
    public void getLockTest() throws InterruptedException {
        RecyclableLock<String ,Object> recyclableLock = new WeakReferenceRecyclableLock<>();
        Object lock = recyclableLock.getLock("lockKey0", Object::new);
        synchronized (lock){
            // 代码
        }

        String key1 = "001";
        Object lock0 = recyclableLock.getLock(key1 ,Object::new);
        String lockAddress0 = lock0.toString();

        System.gc();
        Thread.sleep(1000);

        Object lock1 = recyclableLock.getLock(key1 ,Object::new);
        Assert.assertTrue(lock0 == lock1);

        lock0 = null;
        lock1 = null;
        System.gc();
        Thread.sleep(1000);

        Object lock2 = recyclableLock.getLock(key1 ,Object::new);
        String lockAddress2 = lock2.toString();
        Assert.assertNotEquals(lockAddress0 ,lockAddress2);
    }

}