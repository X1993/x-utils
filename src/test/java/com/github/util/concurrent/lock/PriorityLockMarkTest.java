package com.github.util.concurrent.lock;

import org.junit.Assert;
import org.junit.Test;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class PriorityLockMarkTest {

    @Test
    public void test() throws InterruptedException
    {
        PreemptivePriorityLock<String> preemptivePriorityLock = new PreemptivePriorityLock<>();
        AtomicInteger lockerCount = new AtomicInteger();
        int threadCount = 5;
        CountDownLatch countDownLatch = new CountDownLatch(threadCount);

        int i = 0;
        for (; i < threadCount; i++) {
            int finalI = i;
            new Thread(() -> {
                Locker<String> locker = new Locker<>();
                locker.setKey("locker" + finalI);
                if (preemptivePriorityLock.tryLock(locker)){
                    lockerCount.incrementAndGet();
                }
                countDownLatch.countDown();
            }).start();
        }

        countDownLatch.await();
        Assert.assertEquals(1 ,lockerCount.get());

        Locker<String> locker = new Locker<>();
        locker.setKey("locker" + i++);
        locker.setPriority(1);
        Assert.assertTrue(preemptivePriorityLock.tryLock(locker));

        locker.setPriority(0);
        Assert.assertTrue(preemptivePriorityLock.tryLock(locker));
        Assert.assertTrue(preemptivePriorityLock.getLocker().getPriority() == 0);


        locker = new Locker<>();
        locker.setKey("locker" + i++);
        locker.setPriority(-1);
        Assert.assertFalse(preemptivePriorityLock.tryLock(locker));
    }

}