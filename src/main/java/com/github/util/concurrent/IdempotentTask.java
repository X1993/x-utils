package com.github.util.concurrent;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 通知触发幂等任务
 *
 * 保证每次{@link #call()}调用之后必然会执行一次{@link #task}（不一定由当前线程执行），多个{@link #call()}调用可能
 * 对应到一次{@link #task}执行
 *
 *  例如：监听变化，一种实现是周期轮询，如果变化频率小，那么执行效率低。另外一种实现是通知唤醒task执行，
 *  如果{@link #task}执行的比较慢，可能执行n次{@link #call()}调用后{@link #task}才执行完一次，此时
 *  n-1次{@link #task}已经没有执行的必要。此时使用当前方案许是一种更好的实现
 *
 * @Author: X1993
 * @Date: 2021/4/24
 */
public class IdempotentTask<R> implements Callable<R>{

    /**
     * 需要执行的任务（支持幂等性）
     */
    private final Callable<R> task;

    /**
     * 独占标记
     */
    private final AtomicBoolean exclusive = new AtomicBoolean();

    /**
     * 刷新标记
     */
    private volatile boolean refresh = false;

    /**
     * 如果抢占失败，是否将标记刷新
     */
    private boolean refreshOverExclusive = true;

    public IdempotentTask(Callable<R> task) {
        Objects.requireNonNull(task);
        this.task = task;
    }

    public IdempotentTask(Runnable task)
    {
        Objects.requireNonNull(task);
        this.task = () -> {
            task.run();
            return null;
        };
    }

    public boolean isRefreshOverExclusive() {
        return refreshOverExclusive;
    }

    public void setRefreshOverExclusive(boolean refreshOverExclusive) {
        this.refreshOverExclusive = refreshOverExclusive;
    }

    @Override
    public R call() throws Exception
    {
        do {
            if (exclusive.compareAndSet(false ,true)) {
                //多次通知可以合并执行
                refresh = false;
                try {
                    return task.call();
                }finally {
                    exclusive.set(false);
                }
            }else if (refreshOverExclusive){
                //抢占失败，通知当前任务执行线程
                refresh = true;
                if (exclusive.get()){
                    //另一个线程还在执行中
                    return null;
                }
                //可能在更新刷新标记的时候另一个线程已经结束了
            }
        }
        //执行任务时有更新，重新执行一次
        while (refresh);

        return null;
    }

}
