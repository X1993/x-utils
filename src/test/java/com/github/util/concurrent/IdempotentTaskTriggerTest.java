package com.github.util.concurrent;

import org.junit.Assert;
import org.junit.Test;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class IdempotentTaskTriggerTest {

    @Test
    public void test0() throws InterruptedException
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

    IdempotentTaskTrigger idempotentTask = new IdempotentTaskTrigger(new Runnable() {

        private final Map<Thread ,AtomicInteger> runCountMap = new ConcurrentHashMap<>();

        @Override
        public void run() {
            System.out.println(MessageFormat.format("{0}执行" ,Thread.currentThread()));
            if ((runCountMap.computeIfAbsent(Thread.currentThread() ,
                    x -> new AtomicInteger()).incrementAndGet() & 1) == 0){
                //嵌套执行一次
                return;
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            idempotentTask.run();
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    });

    @Test
    public void test1() throws InterruptedException {
        new Thread(() -> {
            idempotentTask.run();
        }).start();

        new Thread(() -> {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            idempotentTask.run();
        }).start();

        Thread.sleep(1000);
    }

}