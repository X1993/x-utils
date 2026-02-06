package com.github.util.lamdba;

import com.github.util.lamdba.XSupplier;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author wangjj7
 * @date 2026/2/5
 * @description
 */
public class XCacheSupplier<T> implements XSupplier<T> {

    //用Optional包一层区分没执行和执行结果为null
    private volatile Optional<T> cacheResult;

    private final Supplier<T> supplier;

    public XCacheSupplier(Supplier<T> supplier) {
        if (supplier == null){
            throw new IllegalArgumentException("supplier is required");
        }
        this.supplier = supplier;
    }

    public static <T> XCacheSupplier<T> of(Supplier<T> supplier){
        return new XCacheSupplier<>(supplier);
    }

    @Override
    public T get() {
        if (cacheResult != null){
            return cacheResult.orElse(null);
        }
        synchronized (this) {
            if (cacheResult != null){
                return cacheResult.orElse(null);
            }
            T t = supplier.get();
            cacheResult = Optional.ofNullable(t);
            return t;
        }
    }

}
