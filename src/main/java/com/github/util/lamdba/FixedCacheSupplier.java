package com.github.util.lamdba;

import lombok.Data;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * 支持短期缓存的Function函数
 * @author X1993
 * @date 2024/9/4
 * @description
 */
public class FixedCacheSupplier<V> implements Supplier<V> {

    //过期时长，毫秒
    private final long expireMS;

    //查询函数
    private final Supplier<V> valueQuery;

    //最近一次查询时间戳
    private AtomicReference<ValueHolder<V>> lastedValueHolderReference = new AtomicReference<>();

    public FixedCacheSupplier(long expireMS ,Supplier<V> valueQuery) {
        if (expireMS <= 0 || valueQuery == null){
            throw new IllegalArgumentException();
        }
        this.expireMS = expireMS;
        this.valueQuery = valueQuery;
    }

    @Override
    public V get() {
        while (true) {
            long currentTimeMillis = System.currentTimeMillis();
            ValueHolder<V> lastedValueHolder = lastedValueHolderReference.get();
            if (lastedValueHolder != null && currentTimeMillis - lastedValueHolder.lastedQueryTimeMillis <= expireMS){
                return lastedValueHolder.value;
            }
            V value = valueQuery.get();
            if (lastedValueHolderReference.compareAndSet(lastedValueHolder, new ValueHolder<>(currentTimeMillis, value))) {
                return value;
            }
        }
    }

    @Data
    private class ValueHolder<V>{

        //最近一次查询时间戳
        private final long lastedQueryTimeMillis;

        //缓存的值
        private final V value;

    }
    
}
