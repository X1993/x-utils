package com.github.util.concurrent;

import org.junit.Test;

import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;

/**
 * @author wangjj7
 * @date 2024/8/27
 * @description
 */
public class FrequencyLimitSupplierTest {

    @Test
    public void test() throws InterruptedException {
        FrequencyLimitSupplier frequencyLimitSupplier = new FrequencyLimitSupplier(
                100 ,1000 , p -> {
            System.out.println(LocalDateTime.now() + " ,thread:" + Thread.currentThread());
            return p;
        });
        CountDownLatch latch = new CountDownLatch(9);
        for (int i = 0; i < 3; i++) {
            new Thread(() -> {
                for (int j = 0; j < 3; j++){
                    frequencyLimitSupplier.apply("");
                    latch.countDown();
                }
            }).start();
        }

        latch.await();
    }

}