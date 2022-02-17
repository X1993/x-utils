package com.github.util.concurrent;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 通知触发幂等任务
 *
 * 特性：
 * 1.保证时间上每次{@link #run()}调用之后必然会执行一次{@link #task}（不一定由当前线程执行）
 * 2.总执行次数N1小于等于调用次数N0，
 * 3.任一时刻最多只有一个线程在执行
 *
 *  应用场景举例：
 *      监听变化，一种实现是周期轮询，如果变化频率小，那么执行效率低。另外一种实现是通知唤醒task执行，
 *  如果{@link #task}执行的比较慢，可能执行n次{@link #run()}调用后{@link #task}连一次都没执行完，
 *  此时剩下的n-1次{@link #task}已经没有执行的必要。此时使用当前方案许是一种更好的选择
 *
 * @Author: X1993
 * @Date: 2021/4/24
 */
public class IdempotentTask implements Runnable{

    /**
     * 需要执行的任务（支持幂等性）
     */
    private final Runnable task;

    /**
     * 状态管理器
     */
    private final StateManager stateManager;

    public IdempotentTask(Runnable task , StateManager signManager) {
        Objects.requireNonNull(task);
        Objects.requireNonNull(signManager);
        this.task = task;
        this.stateManager = signManager;
    }

    public IdempotentTask(Runnable task)
    {
        this(task ,new LocalStateManager());
    }

    @Override
    public void run() {
        do {
            if (stateManager.tryExclusive()) {
                //可以合并执行
                stateManager.setRefresh(false);
                try {
                    task.run();
                    return;
                }finally {
                    if (!stateManager.releaseExclusive()){
                        throw new IllegalStateException();
                    }
                }
            }else {
                //抢占失败，通知当前任务执行线程
                stateManager.setRefresh(true);
                if (stateManager.isExclusive()){
                    //另一个线程还在执行中
                    return;
                }
                //可能在更新刷新标记的时候另一个线程已经结束了
            }
        }
        //执行任务的过程中有更新，重新执行一次
        while (stateManager.getRefresh());
    }

    /**
     * 状态管理器
     */
    public interface StateManager {

        /**
         * 尝试设置独占状态
         * @return
         */
        boolean tryExclusive();

        /**
         * 解除独占状态
         */
        boolean releaseExclusive();

        /**
         * 是否存在独占状态
         * @return
         */
        boolean isExclusive();

        /**
         * 设置刷新状态
         * @param refreshSign
         */
        void setRefresh(boolean refreshSign);

        /**
         * 获取刷新状态
         * @return
         */
        boolean getRefresh();

    }

    /**
     * 本地（进程内）状态管理器，可以确保同一个进程内实现 {@link IdempotentTask} 要达到的效果
     */
    private static class LocalStateManager implements StateManager {

        /**
         * 独占标记
         */
        private final AtomicReference<Thread> exclusiveSign = new AtomicReference();

        /**
         * 刷新标记
         */
        private volatile boolean refreshSign = false;

        @Override
        public boolean tryExclusive() {
            return exclusiveSign.compareAndSet(null ,Thread.currentThread());
        }

        @Override
        public boolean releaseExclusive() {
            return exclusiveSign.compareAndSet(Thread.currentThread() ,null);
        }

        @Override
        public boolean isExclusive() {
            return exclusiveSign.get() != null;
        }

        @Override
        public void setRefresh(boolean refreshSign) {
            this.refreshSign = refreshSign;
        }

        @Override
        public boolean getRefresh() {
            return this.refreshSign;
        }

    }

}
