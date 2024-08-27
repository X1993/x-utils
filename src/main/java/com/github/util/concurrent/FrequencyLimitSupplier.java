package com.github.util.concurrent;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/**
 * 限制某个函数的执行频率
 * @author wangjj7
 * @date 2024/8/27
 * @description
 */
public class FrequencyLimitSupplier<P ,T> implements Function<P ,T> {

    //最小执行间隔，毫秒
    private final int minIntervalMS;

    //超时时间，毫秒
    private final int timeoutMS;

    //需要限制调用频率的函数
    private final Function<P ,T> function;

    //记录最近调用时间
    private final AtomicLong beforeTimestampAtomic = new AtomicLong(System.currentTimeMillis());

    public FrequencyLimitSupplier(int minIntervalMS, int timeoutMS, Function<P, T> function) {
        if (minIntervalMS < 0 || timeoutMS < 0 || function == null){
            throw new IllegalArgumentException();
        }
        this.minIntervalMS = minIntervalMS;
        this.timeoutMS = timeoutMS;
        this.function = function;
    }

    @Override
    public T apply(P p) {
        long startTimestamp = System.currentTimeMillis();
        while (true){
            long currentTimeMillis = System.currentTimeMillis();
            long totalIntervalMS = currentTimeMillis - startTimestamp;
            long beforeTimestamp = beforeTimestampAtomic.get();
            long intervalMS = Math.max(currentTimeMillis - beforeTimestamp ,0);
            if (beforeTimestamp > 0 && intervalMS >= minIntervalMS){
                if (beforeTimestampAtomic.compareAndSet(beforeTimestamp, currentTimeMillis)) {
                    return function.apply(p);
                }else {
                    beforeTimestamp = beforeTimestampAtomic.get();
                    intervalMS = Math.max(currentTimeMillis - beforeTimestamp ,0);
                }
            }
            long waitMS = minIntervalMS - intervalMS;
            if (totalIntervalMS + waitMS >= timeoutMS){
                throw new CallFrequencyExceedingException("call frequency exceeding limit");
            }
            if (waitMS > 0) {
                try {
                    Thread.sleep(waitMS);
                } catch (InterruptedException e) {
                    throw new IllegalStateException(e);
                }
            }
        }
    }
}
