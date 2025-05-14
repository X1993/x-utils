package com.github.util.concurrent;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 幂等任务触发器
 *
 * 特性：
 * 1.保证时间上每次{@link #run()}调用之后必然会执行一次{@link #task}（不一定由当前线程执行）
 * 2.总执行次数N1小于等于调用次数N0，
 * 3.任一时刻最多只有一个线程在执行
 *
 * 应用场景举例：
 *     监听变化，一种实现是周期轮询，如果变化频率小，那么执行效率低。另外一种实现是通知唤醒task执行，
 * 如果{@link #task}执行的比较慢，可能执行n次{@link #run()}调用后{@link #task}连一次都没执行完，
 * 此时剩下的n-1次{@link #task}已经没有执行的必要。此时使用当前方案许是一种更好的选择
 *
 * @Author: X1993
 * @Date: 2021/4/24
 */
public class IdempotentTaskTrigger implements Runnable{

    /**
     * 任务id
     * 分布式场景下需要用到这个taskId
     */
    private final String taskId;

    /**
     * 需要执行的任务（支持幂等性）
     */
    private final Runnable task;

    /**
     * 超时时间，单位毫秒，如果 <= 0，则表示不超时
     */
    private long timeout;

    /**
     * 状态管理器
     */
    private final StateManager stateManager;

    public IdempotentTaskTrigger(String taskId ,Runnable task ,
                                 long timeout ,StateManager signManager) {

        Objects.requireNonNull(taskId);
        Objects.requireNonNull(task);
        Objects.requireNonNull(signManager);
        this.taskId = taskId;
        this.task = task;
        this.timeout = timeout;
        this.stateManager = signManager;
    }

    public IdempotentTaskTrigger(Runnable task)
    {
        this(UUID.randomUUID().toString() ,task ,-1 ,new LocalStateManager());
    }

    @Override
    public void run()
    {
        String holder = taskId;
        while (true){
            if (stateManager.tryOccupy(holder ,timeout)) {
                try {
                    task.run();
                }finally {
                    if (stateManager.release(holder)){
                        return;
                    }
                }
            }else {
                //抢占失败，通知当前任务执行线程
                stateManager.refresh();
                if (stateManager.currentHolder() != null){
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
         * 尝试设置独占状态
         * @param holder 占用者
         * @param timeout 超时时间，单位毫秒，如果 <= 0，则表示不超时
         * @return
         */
        boolean tryOccupy(String holder ,long timeout);

        default boolean tryOccupy(String holder)
        {
            return tryOccupy(holder ,-1);
        }

        /**
         * 解除独占状态
         */
        boolean release(String holder);

        /**
         * 是否存在独占状态
         * @return
         */
        String currentHolder();

        /**
         * 刷新状态
         */
        boolean refresh();

        /**
         * 重置刷新
         */
        boolean resetRefresh(String holder);

        /**
         * 是否有刷新
         * @return
         */
        boolean isRefresh();

    }

    /**
     * 本地（进程内）状态管理器，可以确保同一个进程内实现 {@link IdempotentTaskTrigger} 要达到的效果
     */
    private static class LocalStateManager implements StateManager {

        /**
         * 独占标记
         */
        private final AtomicReference<ExclusiveContent> exclusiveState = new AtomicReference();

        @Override
        public boolean tryOccupy(String holder ,long timeout)
        {
            while (true) {
                long expireTimestamp = timeout <= 0 ? Long.MAX_VALUE : System.currentTimeMillis() + timeout;
                ExclusiveContent exclusiveContent = exclusiveState.get();
                if (exclusiveContent != null) {
                    if (exclusiveContent.getHolder().equals(holder)){
                        if (exclusiveState.compareAndSet(exclusiveContent ,new ExclusiveContent(holder ,false ,expireTimestamp))){
                            return true;
                        }else {
                            continue;
                        }
                    }else {
                        return false;
                    }
                } else {
                    //尝试占用
                    if (exclusiveState.compareAndSet(null ,new ExclusiveContent(holder ,false ,expireTimestamp))){
                        return true;
                    }else {
                        continue;
                    }
                }
            }
        }

        @Override
        public boolean release(String holder)
        {
            while (true) {
                ExclusiveContent exclusiveContent = exclusiveState.get();
                if (exclusiveContent == null || !holder.equals(exclusiveContent.getHolder())) {
                    return true;
                }
                if (exclusiveContent.isRefresh()){
                    //需要再执行一次
                    return false;
                }
                if (exclusiveState.compareAndSet(exclusiveContent, null)){
                    return true;
                }
            }
        }

        @Override
        public String currentHolder() {
            ExclusiveContent exclusiveContent = exclusiveState.get();
            return exclusiveContent != null ? exclusiveContent.getHolder() : null;
        }

        @Override
        public boolean refresh() {
            while (true) {
                ExclusiveContent exclusiveContent = exclusiveState.get();
                if (exclusiveContent == null){
                    return false;
                }
                if (exclusiveContent.refresh || exclusiveState.compareAndSet(exclusiveContent ,
                        new ExclusiveContent(exclusiveContent.getHolder(), true ,exclusiveContent.expireTimestamp))){
                    return true;
                }
            }
        }

        @Override
        public boolean resetRefresh(String holder) {
            while (true) {
                ExclusiveContent exclusiveContent = exclusiveState.get();
                if (exclusiveContent == null){
                    return false;
                }
                if (!exclusiveContent.refresh || exclusiveState.compareAndSet(exclusiveContent ,
                        new ExclusiveContent(exclusiveContent.getHolder(), false ,exclusiveContent.expireTimestamp))){
                    return true;
                }
            }
        }

        @Override
        public boolean isRefresh() {
            ExclusiveContent exclusiveContent = exclusiveState.get();
            return exclusiveContent != null ? exclusiveContent.refresh : false;
        }

        @AllArgsConstructor
        @Data
        private class ExclusiveContent implements Serializable {

            private final String holder;

            private final boolean refresh;

            private final long expireTimestamp;

        }

    }

}
