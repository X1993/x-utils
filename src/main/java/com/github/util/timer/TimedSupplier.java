package com.github.util.timer;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * 周期性轮询某个结果，直至返回结果或者超时
 * @author wangjj7
 * @date 2024/6/19
 * @description
 */
public class TimedSupplier<T> {

    private final Supplier<T> supplier;

    private final IntervalStrategy intervalStrategy;

    public TimedSupplier(Supplier<T> supplier ,IntervalStrategy intervalStrategy) {
        Objects.requireNonNull(supplier);
        Objects.requireNonNull(intervalStrategy);
        this.supplier = supplier;
        this.intervalStrategy = intervalStrategy;
    }

    public T get()
    {
        int pollingCount = 0;
        long intervalMS = 0L;
        long currentTimeMillis = System.currentTimeMillis();
        while ((intervalMS = intervalStrategy.nextInterval(pollingCount++, intervalMS ,currentTimeMillis)) >= 0) {
            try {
                Thread.sleep(intervalMS);
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
            T t = supplier.get();
            if (t != null){
                return t;
            }
        }
        return null;
    }

}
