package com.github.util.concurrent;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
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
     * 回收队列
     */
    private final ReferenceQueue<L> referenceQueue = new ReferenceQueue<>();

    /**
     * 锁引用集合
     * key:锁标识
     */
    private final Map<K ,LockReference<K ,L>> lockReferences = new HashMap<>();

    /**
     * 回收逻辑
     */
    private final IdempotentTaskTrigger recycleReferenceRunnable = new IdempotentTaskTrigger(() -> recycleReference());

    /**
     * 如果特定标识的锁已存在则返回，否则创建新的锁返回
     * @param lockKey 锁标识
     * @param lockSupplier 锁创建方式
     * @return
     */
    @Override
    public L getLock(K lockKey, Supplier<L> lockSupplier)
    {
        //回收释放的锁
        recycleReferenceRunnable.run();

        L lock = getLockIfExist(lockKey);
        if (lock != null){
            return lock;
        }

        synchronized (lockReferences){
            lock = getLockIfExist(lockKey);
            if (lock != null){
                return lock;
            }

            lock = lockSupplier.get();
            lockReferences.put(lockKey ,new LockReference(lockKey, lock ,referenceQueue));
            return lock;
        }
    }

    /**
     * 如果指定的锁已创建则返回，否则返回null
     * @param lockKey 锁标识
     * @return
     */
    private L getLockIfExist(K lockKey){
        LockReference<K ,L> lockReference = lockReferences.get(lockKey);
        if (lockReference != null){
            L lock = lockReference.get();
            if (lock != null){
                return lock;
            }
        }
        return null;
    }

    /**
     * 释放不再引用的锁相关资源
     */
    private void recycleReference()
    {
        Reference reference = null;
        while ((reference = referenceQueue.poll()) != null){
            LockReference<K ,L> lockReference = (LockReference<K ,L>) reference;
            K lockKey = lockReference.getLockKey();
            if (getLockIfExist(lockKey) != null){
                continue;
            }

            synchronized (lockReferences){
                if (getLockIfExist(lockKey) != null){
                    continue;
                }
                lockReferences.remove(lockKey);
            }
        }
    }

    static class LockReference<K ,L> extends WeakReference<L> {

        //锁标识
        private final K lockKey;

        public LockReference(K lockKey , L referent , ReferenceQueue<L> referenceQueue) {
            super(referent ,referenceQueue);
            this.lockKey = lockKey;
        }

        public K getLockKey() {
            return lockKey;
        }

    }

}
