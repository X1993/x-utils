package com.github.util.concurrent;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.Executors;

/**
 * @author wangjj7
 * @date 2023/5/5
 * @description
 */
public class ConcurrentUtilsTest {

    @Test
    public void testParallel() {
        long startTimeMillis = System.currentTimeMillis();
        ConcurrentUtils.parallel(Executors.newFixedThreadPool(2) ,
                mockTask(500) ,
                mockTask(500) ,
                mockTask(500));
        long timeConsuming = System.currentTimeMillis() - startTimeMillis;
        Assert.assertTrue(timeConsuming < 600);

        long startTimeMillis1 = System.currentTimeMillis();
        ConcurrentUtils.parallel(Executors.newFixedThreadPool(1) ,
                mockTask(500) ,
                mockTask(500) ,
                mockTask(500));
        long timeConsuming1 = System.currentTimeMillis() - startTimeMillis1;
        Assert.assertTrue(timeConsuming1 >= 1000);
    }

    private Runnable mockTask(long timeConsuming){
        return () -> {
            try {
                Thread.sleep(timeConsuming);
            } catch (InterruptedException e) {

            }
        };
    }

}