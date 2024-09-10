package com.github.util.concurrent;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * @author wangjj7
 * @date 2024/9/10
 * @description
 */
public class MergeFuture<V> implements Future<List<V>> {

    private final Future<V>[] futures;

    public MergeFuture(Future<V>[] futures) {
        if (futures == null || futures.length <= 0){
            throw new IllegalArgumentException();
        }
        this.futures = futures;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return Arrays.stream(futures).allMatch(future -> future.cancel(mayInterruptIfRunning));
    }

    @Override
    public boolean isCancelled() {
        return Arrays.stream(futures).allMatch(Future::isCancelled);
    }

    @Override
    public boolean isDone() {
        return Arrays.stream(futures).allMatch(Future::isDone);
    }

    @Override
    public List<V> get() throws InterruptedException, ExecutionException {
        return Arrays.stream(futures)
                .map(future -> {
                    try {
                        return future.get();
                    } catch (InterruptedException | ExecutionException e) {
                        throw new IllegalStateException(e);
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<V> get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return Arrays.stream(futures)
                .map(future -> {
                    try {
                        return future.get(timeout, unit);
                    } catch (InterruptedException | ExecutionException | TimeoutException e) {
                        throw new IllegalStateException(e);
                    }
                })
                .collect(Collectors.toList());
    }

    public Future<V>[] getFutures() {
        return futures;
    }

}
