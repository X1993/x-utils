package com.github.util.concurrent;

import com.github.util.structure.WeakValueHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 可回收锁
 * 回收策略：锁对象不可达（没用引用关系）时回收
 * <p>
 *     //使用方式
 *     RecyclableLock<String ,Object> recyclableLock = new WeakReferenceLock<>();
 *     Object lock = recyclableLock.getLock("lockKey0", Object::new);
 *     synchronized (lock){
 *         // 代码
 *     }
 * </p>
 * @author X1993
 * @date 2022/11/25
 * @description
 */
public class WeakReferenceLock<K ,L> implements RecyclableLock<K ,L> {

    /**
     * 锁引用集合
     * key:锁标识
     */
    private final Map<K ,L> locks = new WeakValueHashMap<>();

    /**
     * 如果特定标识的锁已存在则返回，否则创建新的锁返回
     * @param lockKey 锁标识
     * @param lockSupplier 锁创建方式
     * @return
     */
    @Override
    public L getLock(K lockKey, Supplier<L> lockSupplier)
    {
        L lock = locks.get(lockKey);
        if (lock != null){
            return lock;
        }

        synchronized (locks){
            lock = locks.get(lockKey);
            if (lock != null){
                return lock;
            }

            lock = lockSupplier.get();
            locks.put(lockKey ,lock);
            return lock;
        }
    }

}
