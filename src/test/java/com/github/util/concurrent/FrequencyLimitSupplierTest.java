package com.github.util.concurrent;

import org.junit.Test;

import java.time.LocalDateTime;

/**
 * @author wangjj7
 * @date 2024/8/27
 * @description
 */
public class FrequencyLimitSupplierTest {

    @Test
    public void test(){
        FrequencyLimitSupplier frequencyLimitSupplier = new FrequencyLimitSupplier(
                1000 ,5000 , p -> {
            System.out.println(p);
            return p;
        });
        for (int i = 0; i < 3; i++) {
            frequencyLimitSupplier.apply(LocalDateTime.now() + "," + i);
        }
    }

}