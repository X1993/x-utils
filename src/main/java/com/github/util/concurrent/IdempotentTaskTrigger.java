package com.github.util.concurrent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 幂等任务触发器
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
public class IdempotentTaskTrigger implements Runnable{

    /**
     * 需要执行的任务（支持幂等性）
     */
    private final Runnable task;

    /**
     * 状态管理器
     */
    private final StateManager stateManager;

    public IdempotentTaskTrigger(Runnable task ,StateManager signManager) {
        Objects.requireNonNull(task);
        Objects.requireNonNull(signManager);
        this.task = task;
        this.stateManager = signManager;
    }

    public IdempotentTaskTrigger(Runnable task)
    {
        this(task ,new LocalStateManager());
    }

    @Override
    public void run()
    {
        while (true){
            if (stateManager.tryOccupy()) {
                //可以合并执行
                stateManager.resetRefresh();
                try {
                    task.run();
                }finally {
                    if (!stateManager.isRefresh()) {
                        if (!stateManager.release()) {
                            //不应该执行到这里，有bug！🤦‍
                            throw new IllegalStateException();
                        }
                        if (!stateManager.isRefresh()) {
                            //确保在解除占用标记的过程中没有新的执行请求
                            return;
                        }
                    }
                    //任务执行过程中有收到新的执行请求，重新执行一次
                }
            }else {
                //抢占失败，通知当前任务执行线程
                stateManager.refresh();
                if (stateManager.isExclusive()){
                    //确保执行中的线程能收到通知
                    return;
                }
                //可能在更新刷新标记的时候另一个线程已经结束了，再次尝试获取执行权
            }
        }
    }

    /**
     * 状态管理器
     */
    public interface StateManager {

        /**
         * 尝试设置独占状态，可重入
         * @return
         */
        boolean tryOccupy();

        /**
         * 解除独占状态
         */
        boolean release();

        /**
         * 是否存在独占状态
         * @return
         */
        boolean isExclusive();

        /**
         * 刷新次数加一
         */
        int refresh();

        /**
         * 重置刷新次数
         */
        void resetRefresh();

        /**
         * 获取刷新次数
         * @return
         */
        int refreshCount();

        /**
         * 是否有刷新
         * @return
         */
        default boolean isRefresh(){
            return refreshCount() > 0;
        }

    }

    /**
     * 本地（进程内）状态管理器，可以确保同一个进程内实现 {@link IdempotentTaskTrigger} 要达到的效果
     */
    private static class LocalStateManager implements StateManager {

        /**
         * 独占标记
         */
        private final AtomicReference<ExclusiveContent> exclusiveState = new AtomicReference();

        /**
         * 刷新标记
         */
        private final AtomicInteger refreshCount = new AtomicInteger();

        @Override
        public boolean tryOccupy()
        {
            Thread thread = Thread.currentThread();
            while (true) {
                ExclusiveContent exclusiveContent = exclusiveState.get();
                if (exclusiveContent != null) {
                    if (exclusiveContent.getThread() == thread) {
                        //重入
                        if (exclusiveState.compareAndSet(exclusiveContent,
                                new ExclusiveContent(thread, exclusiveContent.reentriesCount + 1))){
                            return true;
                        }
                    } else {
                        //被占用
                        return false;
                    }
                } else {
                    //尝试占用
                    if (exclusiveState.compareAndSet(null ,new ExclusiveContent(thread ,1))){
                        return true;
                    }
                }
            }
        }

        @Override
        public boolean release()
        {
            while (true) {
                ExclusiveContent exclusiveContent = exclusiveState.get();
                if (exclusiveContent == null) {
                    return true;
                }
                Thread occupyThread = exclusiveContent.getThread();
                Thread currentThread = Thread.currentThread();
                if (currentThread != occupyThread) {
                    return false;
                }
                int reentriesCount = exclusiveContent.getReentriesCount();
                if (reentriesCount > 1) {
                    if (exclusiveState.compareAndSet(exclusiveContent,
                            new ExclusiveContent(currentThread, reentriesCount - 1))){
                        return true;
                    }
                } else {
                    if (exclusiveState.compareAndSet(exclusiveContent, null)){
                        return true;
                    }
                }
            }
        }

        @Override
        public boolean isExclusive() {
            return exclusiveState.get() != null;
        }

        @Override
        public int refresh() {
            return refreshCount.incrementAndGet();
        }

        @Override
        public void resetRefresh() {
            refreshCount.set(0);
        }

        @Override
        public int refreshCount() {
            return refreshCount.get();
        }

        @NoArgsConstructor
        @AllArgsConstructor
        @Data
        private class ExclusiveContent{

            private Thread thread;

            private int reentriesCount;

        }

    }

}
