package com.github.util.concurrent;

import java.util.function.Supplier;

/**
 * 可回收锁
 * @author X1993
 * @date 2022/11/26
 * @description
 */
public interface RecyclableLock<K ,L> {

    /**
     * 如果特定标识的锁已存在则返回，否则创建新的锁返回
     * @param lockKey 锁标识
     * @param lockSupplier 锁创建方式
     * @return
     */
    L getLock(K lockKey, Supplier<L> lockSupplier);

}
