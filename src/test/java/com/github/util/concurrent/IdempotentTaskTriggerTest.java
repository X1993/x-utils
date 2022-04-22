package com.github.util.concurrent;

import org.junit.Assert;
import org.junit.Test;
import java.util.concurrent.atomic.AtomicInteger;

public class IdempotentTaskTriggerTest {

    @Test
    public void run() throws InterruptedException
    {
        AtomicInteger counter = new AtomicInteger();
        long sleepMs = 500L;
        IdempotentTaskTrigger idempotentTask = new IdempotentTaskTrigger(() -> {
            try {
                Thread.sleep(sleepMs);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            counter.incrementAndGet();
        });

        for (int i = 0; i < 5; i++) {
            new Thread(idempotentTask).start();
        }

        Thread.sleep(3 * sleepMs);
        Assert.assertTrue(counter.get() == 2);
    }

}