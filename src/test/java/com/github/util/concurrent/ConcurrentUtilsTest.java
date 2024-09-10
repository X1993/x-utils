package com.github.util.concurrent;

import org.junit.Assert;
import org.junit.Test;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author wangjj7
 * @date 2023/5/5
 * @description
 */
public class ConcurrentUtilsTest {

    @Test
    public void testParallel() throws ExecutionException, InterruptedException {
        long startTimeMillis = System.currentTimeMillis();
        Future future = ConcurrentUtils.parallel(Executors.newFixedThreadPool(2),
                mockTask(500),
                mockTask(500),
                mockTask(500));
        future.get();
        long timeConsuming = System.currentTimeMillis() - startTimeMillis;
        System.out.println("测试1总耗时" + timeConsuming + "MS");
        Assert.assertTrue(timeConsuming >= 500 && timeConsuming < 1000);
        System.out.println("-------------测试1通过-------------");

        long startTimeMillis1 = System.currentTimeMillis();
        future = ConcurrentUtils.parallel(Executors.newFixedThreadPool(1) ,
                mockTask(500) ,
                mockTask(500) ,
                mockTask(500));
        future.get();
        long timeConsuming1 = System.currentTimeMillis() - startTimeMillis1;
        System.out.println("测试2总耗时" + timeConsuming1 + "MS");
        Assert.assertTrue(timeConsuming1 >= 1000 && timeConsuming1 < 1500);

        ConcurrentUtils.parallel(null ,mockTask(500));
    }

    private Runnable mockTask(long timeConsuming){
        return () -> {
            try {
                System.out.println(MessageFormat.format("{0}，{1}线程执行,耗时:{2}ms" ,
                        LocalDateTime.now() ,Thread.currentThread() ,timeConsuming));
                Thread.sleep(timeConsuming);
            } catch (InterruptedException e) {

            }
        };
    }

}