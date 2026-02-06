package com.github.util.lamdba;

import org.junit.Assert;
import org.junit.Test;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author wangjj7
 * @date 2024/9/5
 * @description
 */
public class XFixedCacheSupplierTest {

    @Test
    public void getTest() throws InterruptedException {
        AtomicInteger atomicInteger = new AtomicInteger();
        XFixedCacheSupplier<Integer> XFixedCacheSupplier = new XFixedCacheSupplier<>(
                100 ,() -> atomicInteger.getAndIncrement());
        Assert.assertTrue(XFixedCacheSupplier.get() == 0);
        Assert.assertTrue(XFixedCacheSupplier.get() == 0);
        Thread.sleep(110);
        Assert.assertTrue(XFixedCacheSupplier.get() == 1);
        Thread.sleep(30);
        Assert.assertTrue(XFixedCacheSupplier.get() == 1);
        Thread.sleep(100);
        Assert.assertTrue(XFixedCacheSupplier.get() == 2);
    }

}