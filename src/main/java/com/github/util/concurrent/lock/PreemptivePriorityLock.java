package com.github.util.concurrent.lock;

import java.util.concurrent.atomic.AtomicReference;

/**
 * 可抢占的优先级锁，并不是传统意义上的阻塞线程，仅仅记录锁的占有者，如果支持高优先级抢占
 * {@link PreemptivePriorityLock#preemptive} == true，所以即使某个线程获取了锁，也有
 * 可能会在unlock前失去锁，需要在代码中配合检查点使用（当然也可以改造成传统的阻塞模式）
 *
 * @Author: jie
 * @Date: 2021/8/6
 */
public class PreemptivePriorityLock<K> {

    /**
     * 当前占用锁的人
     */
    private final AtomicReference<Locker> lockMark = new AtomicReference<>();

    /**
     * 是否允许高优先级Locker抢占锁资源
     */
    private boolean preemptive = true;

    /**
     * 返回占用了锁的人
     * @return
     */
    public Locker<K> getLocker()
    {
        return lockMark.get();
    }

    /**
     * 尝试抢占锁
     * @param locker
     * @return
     */
    public boolean tryLock(Locker<K> locker)
    {
        if (locker == null){
            return false;
        }

        while (true)
        {
            Locker<K> oldLocker = getLocker();
            if (oldLocker == null){
                if (lockMark.compareAndSet(null ,locker)) {
                    return true;
                }
            }else if (oldLocker.getKey().equals(locker.getKey())){
                //重入
                oldLocker.setPriority(locker.getPriority());
                return true;
            }else if (preemptive && locker.getPriority() > oldLocker.getPriority()) {
                // 更高优先级
                if (lockMark.compareAndSet(oldLocker ,locker)){
                    Locker.PreemptCallback preemptCallback = oldLocker.getPreemptCallback();
                    if (preemptCallback != null){
                        preemptCallback.preempt(locker);
                    }
                    return true;
                }
            }else {
                //加锁失败
                return false;
            }
        }
    }

    /**
     * 解锁
     * @param lockKey 解锁人
     * @return
     */
    public boolean unlock(K lockKey)
    {
        if (lockKey == null){
            return false;
        }
        Locker oldLocker = null;
        while (lockKey.equals(oldLocker = getLocker())){
            if (lockMark.compareAndSet(oldLocker ,null)){
                return true;
            }
        }
        return false;
    }

    public boolean isPreemptive() {
        return preemptive;
    }

    public void setPreemptive(boolean preemptive) {
        this.preemptive = preemptive;
    }

}
