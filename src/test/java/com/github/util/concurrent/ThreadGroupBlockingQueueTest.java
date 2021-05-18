package com.github.util.concurrent;

import org.junit.Assert;
import org.junit.Test;
import java.util.Map;
import java.util.concurrent.*;

public class ThreadGroupBlockingQueueTest{

    @Test
    public void test()
    {
        int subQueueCount = 3;
        int coreThreadCount = subQueueCount + 3;

        BlockingQueue[] threadQueueGroup = new BlockingQueue[subQueueCount];
        for (int i = 0; i < threadQueueGroup.length; i++) {
            threadQueueGroup[i] = new LinkedBlockingQueue<>();
        }

        ThreadGroupBlockingQueue<Integer> queue = new ThreadGroupBlockingQueue<Integer>(
                threadQueueGroup, (i ,s) -> i.intValue() % s);
        final Map<Thread ,BlockingQueue<Integer>> results = new ConcurrentHashMap<>();

        for (int i = 0; i < coreThreadCount; i++) {
            new Thread(() -> {
                while (true){
                    Integer e = null;
                    try {
                        e = queue.poll(Long.MAX_VALUE , TimeUnit.SECONDS);
                    } catch (InterruptedException interruptedException) {
                        continue;
                    }
                    if (e != null) {
                        System.out.println(Thread.currentThread() + ",print:" + e);
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException interruptedException) {
                            interruptedException.printStackTrace();
                        }
                        results.computeIfAbsent(Thread.currentThread(), t -> new LinkedBlockingQueue()).add(e);
                    }
                }
            }).start();
        }

//        Thread.sleep(100);
        int subQueueSize = 100;
        int taskCount = subQueueCount * subQueueSize;
        for (int i = 0; i < taskCount; i++) {
            queue.offer(i);
        }

        while (results.values().stream().mapToInt(list -> list.size()).sum() < taskCount);

        for (BlockingQueue<Integer> blockingQueue : results.values())
        {
            if (blockingQueue.size() > 0){
                Assert.assertTrue(blockingQueue.size() == subQueueSize);
                int i = blockingQueue.poll() % subQueueCount;
                Integer x = null;
                while ((x = blockingQueue.poll()) != null){
                    Assert.assertTrue(x.intValue() % subQueueCount == i);
                }
            }
        }
    }

}