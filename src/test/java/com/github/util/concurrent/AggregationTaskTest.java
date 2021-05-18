package com.github.util.concurrent;

import org.junit.Assert;
import org.junit.Test;
import java.util.concurrent.atomic.AtomicInteger;

public class AggregationTaskTest {

    @Test
    public void run() throws InterruptedException
    {
        AtomicInteger counter = new AtomicInteger();
        long sleepMs = 500L;
        AggregationTask aggregationTask = new AggregationTask(() -> {
            try {
                Thread.sleep(sleepMs);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            counter.incrementAndGet();
        });

        new Thread(() -> {
            for (int j = 0; j < 5; j++) {
                aggregationTask.run();
            }
        }).start();

        Thread.sleep(3 * sleepMs);
        Assert.assertTrue(counter.get() == 2);
    }

}