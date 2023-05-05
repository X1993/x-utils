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
     * @param executor
     * @param originalTasks
     * @return
     */
    public static Future[] parallel(Executor executor ,Runnable... originalTasks) {
        if (originalTasks == null || originalTasks.length == 0){
            return new Future[0];
        }

        int length = originalTasks.length;

        XRunnable[] xTasks = new XRunnable[length];

        for (int i = 0; i < length; i++) {
            xTasks[i] = new XRunnable(originalTasks[i]);
        }

        return parallel(executor ,xTasks);
    }

    /**
     * 多个任务并行
     * @param executor
     * @param originalTasks
     * @return
     */
    private static Future[] parallel(Executor executor ,Callable... originalTasks) {
        if (originalTasks == null || originalTasks.length == 0){
            return new Future[0];
        }

        int length = originalTasks.length;

        XRunnable[] xTasks = new XRunnable[length];

        for (int i = 0; i < length; i++) {
            xTasks[i] = new XRunnable(originalTasks[i]);
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
    public static Future[] parallel(Executor executor ,XRunnable... xTasks)
    {
        if (xTasks == null || xTasks.length == 0){
            return new Future[0];
        }

        int taskCount = xTasks.length;

        if (taskCount > 1) {
            if (executor == null){
                throw new IllegalArgumentException();
            }
            //留一个任务给当前线程，充分利用线程资源
            for (int i = 1; i < taskCount; i++) {
                executor.execute(xTasks[i]);
            }
        }

        xTasks[0].run();

        for (int i = 1; i < taskCount; i++) {
            //如果有任务还没开始执行，由当前线程执行
            xTasks[i].run();
        }

        Future[] futures = Arrays.stream(xTasks)
                .map(xTask -> xTask.getFuture())
                .toArray(len -> new Future[len]);

        for (Future future : futures) {
            if (!future.isDone()){
                try {
                    future.get();
                } catch (InterruptedException | CancellationException | ExecutionException e) {
                    continue;
                }
            }
        }

        return futures;
    }

}
