package com.github.util.lamdba;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author wangjj7
 * @date 2024/6/19
 * @description
 */
@FunctionalInterface
public interface XSupplier<T> extends Supplier<T> {

    default <V> XSupplier<V> doThen(Function<T ,V> function){
        return () -> {
            T t = XSupplier.this.get();
            return t != null ? function.apply(t) : null;
        };
    }

    /**
     * 带缓存的{@link XSupplier}
     * @return
     */
    default XSupplier<T> cache(){
        return LambdaUtils.cache(this);
    }

    static <T> XSupplier<T> start(XSupplier<T> supplier){
        return supplier;
    }

}
