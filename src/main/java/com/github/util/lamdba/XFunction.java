package com.github.util.lamdba;

import java.util.Map;
import java.util.function.Function;

/**
 * @author wangjj7
 * @date 2024/6/19
 * @description
 */
@FunctionalInterface
public interface XFunction<T ,R> extends Function<T ,R> {

    default <R2> XFunction<T ,R2> doThen(Function<R ,R2> function){
        return t -> {
            R r = XFunction.this.apply(t);
            return r != null ? function.apply(r) : null;
        };
    }

    /**
     * 带缓存的{@link XSupplier}
     * @param cacheMap
     * @return
     */
    default XFunction<T ,R> cache(Map<T ,R> cacheMap){
        return LambdaUtils.cache(this ,cacheMap);
    }

}
