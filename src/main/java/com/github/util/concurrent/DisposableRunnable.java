package com.github.util.concurrent;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author X1993
 * @date 2023/5/5
 * @description
 */
public class DisposableRunnable<T> implements Runnable{

    private final Runnable target;

    private final CompletableFuture<T> completableFuture = new CompletableFuture();

    //已启动信号
    private final AtomicBoolean startedSignal = new AtomicBoolean(false);

    public DisposableRunnable(Runnable originalTask) {
        this.target = () -> {
            try {
                originalTask.run();
                completableFuture.complete(null);
            }catch (Exception e){
                completableFuture.completeExceptionally(e);
            }
        };
    }

    public DisposableRunnable(Callable<T> originalTask) {
        this.target = () -> {
            try {
                completableFuture.complete(originalTask.call());
            }catch (Exception e){
                completableFuture.completeExceptionally(e);
            }
        };
    }

    @Override
    public void run() {
        if (startedSignal.compareAndSet(false ,true)){
            target.run();//确保每个任务只会执行一次
        }
    }

    public Future<T> getFuture() {
        return completableFuture;
    }
}
