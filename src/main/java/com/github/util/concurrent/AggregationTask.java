package com.github.util.concurrent;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 聚合任务
 *
 * 保证每次{@link #run()}调用之后必然会执行一次{@link #task}（不一定由当前线程执行），多个{@link #run()}调用可能
 * 对应到一次{@link #task}执行
 *
 *  例如：监听变化，一种实现是周期轮询，如果变化频率小，那么执行效率低。另外一种实现是通知唤醒，如果{@link #task}执行的比较慢，
 * 可能执行n次{@link #run()}调用后{@link #task}才执行完一次，此时n-1次{@link #task}已经没有执行的必要。此时使用当前方案
 * 或许是一种更好的实现
 *
 * @Author: X1993
 * @Date: 2021/4/24
 */
public class AggregationTask implements Runnable{

    /**
     * 需要执行的任务（支持幂等性）
     */
    private final Runnable task;

    /**
     * 独占标记
     */
    private final AtomicBoolean exclusive = new AtomicBoolean();

    /**
     * 刷新标记
     */
    private volatile boolean refresh = false;

    public AggregationTask(Runnable task) {
        Objects.requireNonNull(task);
        this.task = task;
    }

    @Override
    public void run() {
        do {
            if (exclusive.compareAndSet(false ,true)) {
                //多次通知可以合并执行
                refresh = false;
                try {
                    task.run();
                }finally {
                    exclusive.set(false);
                }
            }else {
                //抢占失败，通知抢占线程
                refresh = true;
                if (exclusive.get()){
                    //另一个线程还在执行中
                    return;
                }
                //可能在更新刷新标记的时候另一个线程已经结束了
            }
        }
        //执行任务时有更新，重新执行一次
        while (refresh);
    }

}
