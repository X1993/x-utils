package com.github.util.concurrent;

import java.util.Arrays;
import java.util.concurrent.*;

/**
 * @author X1993
 * @date 2023/5/4
 * @description
 */
public class ConcurrentUtils {

    /**
     * 多个任务并行
     * 通常的并发执行流程是主线程等待多个并发线程执行完成，此时主线程处于闲置状态，尽量优化这个流程，避免资源闲置
     * @param executor
     * @param originalTasks
     * @return
     */
    public static MergeFuture<Void> parallel(Executor executor ,Runnable... originalTasks) {
        if (originalTasks == null || originalTasks.length == 0){
            throw new IllegalArgumentException();
        }

        int length = originalTasks.length;

        DisposableRunnable[] xTasks = new DisposableRunnable[length];

        for (int i = 0; i < length; i++) {
            xTasks[i] = new DisposableRunnable(originalTasks[i]);
        }

        return parallel(executor ,xTasks);
    }

    /**
     * 多个任务并行
     * 通常的并发执行流程是主线程等待多个并发线程执行完成，此时主线程处于闲置状态，尽量优化这个流程，避免资源闲置
     * @param executor
     * @param originalTasks
     * @return
     */
    private static <V> MergeFuture<V> parallel(Executor executor ,Callable<V>... originalTasks)
    {
        if (originalTasks == null || originalTasks.length == 0){
            throw new IllegalArgumentException();
        }

        int length = originalTasks.length;

        DisposableRunnable<V>[] xTasks = new DisposableRunnable[length];

        for (int i = 0; i < length; i++) {
            xTasks[i] = new DisposableRunnable(originalTasks[i]);
        }

        return parallel(executor ,xTasks);
    }

    /**
     * 多个任务并行
     * 通常的并发执行流程是主线程等待多个并发线程执行完成，此时主线程处于闲置状态，尽量优化这个流程，避免资源闲置
     * @param executor
     * @param xTasks
     * @return
     */
    public static <V> MergeFuture<V> parallel(Executor executor ,DisposableRunnable<V>... xTasks)
    {
        if (xTasks == null || xTasks.length == 0){
            throw new IllegalArgumentException();
        }

        int taskCount = xTasks.length;

        if (taskCount > 1 && executor != null) {
            //留一个任务给当前线程，充分利用线程资源
            for (int i = 1; i < taskCount; i++) {
                executor.execute(xTasks[i]);
            }
        }

        for (int i = 0; i < taskCount; i++) {
            //如果有任务还没开始执行，由当前线程执行
            xTasks[i].run();
        }

        Future<V>[] futures = Arrays.stream(xTasks)
                .map(xTask -> xTask.getFuture())
                .toArray(len -> new Future[len]);

        return new MergeFuture<>(futures);
    }

}
