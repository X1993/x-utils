package com.github.util.lamdba;

import org.junit.Assert;
import org.junit.Test;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author wangjj7
 * @date 2024/9/5
 * @description
 */
public class FixedCacheSupplierTest {

    @Test
    public void getTest() throws InterruptedException {
        AtomicInteger atomicInteger = new AtomicInteger();
        FixedCacheSupplier<Integer> fixedCacheSupplier = new FixedCacheSupplier<>(
                100 ,() -> atomicInteger.getAndIncrement());
        Assert.assertTrue(fixedCacheSupplier.get() == 0);
        Assert.assertTrue(fixedCacheSupplier.get() == 0);
        Thread.sleep(110);
        Assert.assertTrue(fixedCacheSupplier.get() == 1);
        Thread.sleep(30);
        Assert.assertTrue(fixedCacheSupplier.get() == 1);
        Thread.sleep(100);
        Assert.assertTrue(fixedCacheSupplier.get() == 2);
    }

}